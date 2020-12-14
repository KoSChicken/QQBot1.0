package io.koschicken.listeners;

import com.forte.qqrobot.anno.Filter;
import com.forte.qqrobot.anno.Listen;
import com.forte.qqrobot.beans.cqcode.CQCode;
import com.forte.qqrobot.beans.messages.msgget.GroupMsg;
import com.forte.qqrobot.beans.messages.types.MsgGetTypes;
import com.forte.qqrobot.sender.MsgSender;
import com.forte.qqrobot.utils.CQCodeUtil;
import io.koschicken.bean.Pixiv;
import io.koschicken.database.bean.Scores;
import io.koschicken.database.service.ScoresService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.koschicken.utils.SetuUtils.getSetu;

@Component
public class SetuListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetuListener.class);
    private static final String TEMP = "./temp/SETU/";
    private static final String ARTWORK_PREFIX = "https://www.pixiv.net/artworks/";
    private static final String ARTIST_PREFIX = "https://www.pixiv.net/users/";
    private static final String AVATAR_API = "http://thirdqq.qlogo.cn/g?b=qq&nk=";
    private static final int CD = 20;
    private static final String CQ_AT = "[CQ:at,qq=";
    private static final HashMap<String, Integer> NUMBER;
    private static final double PRICE = 50;
    private static HashMap<String, HashMap<String, LocalDateTime>> coolDown;

    static {
        NUMBER = new HashMap<>();
        NUMBER.put("一", 1);
        NUMBER.put("二", 2);
        NUMBER.put("俩", 2);
        NUMBER.put("两", 2);
        NUMBER.put("三", 3);
        NUMBER.put("四", 4);
        NUMBER.put("五", 5);
        NUMBER.put("六", 6);
        NUMBER.put("七", 7);
        NUMBER.put("八", 8);
        NUMBER.put("九", 9);
        NUMBER.put("十", 10);
        NUMBER.put("几", RandomUtils.nextInt(1, 4));
    }

    static {
        File setuFolder = new File(TEMP);
        if (!setuFolder.exists()) {
            try {
                FileUtils.forceMkdir(setuFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Autowired
    private ScoresService scoresService;

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"叫车(.*)(.*)?(|r18)", "来(.*?)[点丶份张幅](.*?)的?(|r18)[色瑟涩][图圖]"})
    public void jiaoche(GroupMsg msg, MsgSender sender) {
        if (isCool(msg.getQQ(), msg.getGroupCode())) {
            Scores coin = scoresService.getById(msg.getCodeNumber());
            if (coin == null) {
                createScore(msg, sender);
            } else {
                if (coin.getScore() >= PRICE) {
                    String message = msg.getMsg();
                    String regex = message.startsWith("叫车") ? "叫车(.*)(.*)?(|r18)" : "来(.*?)[点丶份张幅](.*?)的?(|r18)[色瑟涩][图圖]";
                    Pattern p = Pattern.compile(regex);
                    Matcher m = p.matcher(message);
                    int num = 1;
                    String tag = "";
                    boolean r18 = false;
                    String number;
                    while (m.find()) {
                        // 兼容原有的叫车功能
                        if (message.startsWith("叫车")) {
                            number = m.group(2).trim();
                            tag = m.group(1).trim();
                        } else {
                            number = m.group(1).trim();
                            tag = m.group(2).trim();
                        }
                        try {
                            num = NUMBER.get(number) == null ? Integer.parseInt(number) : NUMBER.get(number);
                        } catch (NumberFormatException ignore) {
                            LOGGER.info("number set to 1");
                        }
                        r18 = !StringUtils.isEmpty(m.group(3).trim());
                    }
                    // 发图
                    Long qq = scoresService.findQQByNickname(tag);
                    if (qq != null) {
                        groupMember(msg, sender, qq);
                    } else {
                        SendSetu sendSetu = new SendSetu(msg.getGroupCode(), msg.getQQ(), sender, tag, num, r18, coin, scoresService);
                        sendSetu.start();
                        refreshCooldown(msg.getQQ(), msg.getGroupCode());
                    }
                } else {
                    sender.SENDER.sendGroupMsg(msg.getGroupCode(), CQ_AT + msg.getQQCode() + "]" + "你没钱了，请尝试签到或找开发者PY");
                }
            }
        } else {
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "叫车CD中...");
        }
    }

    private void createScore(GroupMsg msg, MsgSender sender) {
        Scores scores = new Scores();
        scores.setSignFlag(false);
        scores.setQq(msg.getQQ());
        scores.setScore(0);
        scoresService.save(scores);
        sender.SENDER.sendGroupMsg(msg.getGroupCode(), CQ_AT + msg.getQQCode() + "]" + "你没钱了，请尝试签到或找开发者PY");
    }

    private void groupMember(GroupMsg msg, MsgSender sender, Long qq) {
        String api = AVATAR_API + qq + "&s=640";
        try {
            InputStream imageStream = Request.Get(api).execute().returnResponse().getEntity().getContent();
            File pic = new File(TEMP + qq + System.currentTimeMillis() + ".jpg");
            FileUtils.copyInputStreamToFile(imageStream, pic);
            CQCode cqCodeImage = CQCodeUtil.build().getCQCode_Image(pic.getAbsolutePath());
            LOGGER.info(pic.getAbsolutePath());
            String message = cqCodeImage.toString();
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), message);
            FileUtils.deleteQuietly(pic);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 刷新冷却时间
     */
    private void refreshCooldown(String qq, String groupCode) {
        LocalDateTime localDateTime = LocalDateTime.now();
        if (coolDown == null) {
            coolDown = new HashMap<>();
        }
        HashMap<String, LocalDateTime> hashMap = coolDown.get(groupCode);
        if (hashMap == null) {
            hashMap = new HashMap<>();
        }
        hashMap.put(qq, localDateTime.plusSeconds(CD));
        coolDown.put(groupCode, hashMap);
    }

    /**
     * 获取冷却时间是不是到了
     *
     * @param QQ
     */
    private boolean isCool(String QQ, String groupCode) {
        if (coolDown == null) {
            coolDown = new HashMap<>();
            return true;
        } else {
            HashMap<String, LocalDateTime> hashMap = coolDown.get(groupCode);
            if (hashMap != null) {
                LocalDateTime localDateTime = hashMap.get(QQ);
                if (localDateTime != null) {
                    return localDateTime.isBefore(LocalDateTime.now());
                } else {
                    return true;
                }
            } else {
                return true;
            }
        }
    }

    static class SendSetu extends Thread {
        private final String groupCode;
        private final String privateQQ;
        private final MsgSender sender;
        private final String tag;
        private final Integer num;
        private final Boolean r18;
        private final Scores coin;
        private final ScoresService scoresService;

        public SendSetu(String groupCode, String privateQQ, MsgSender sender, String tag, Integer num,
                        Boolean r18, Scores coin, ScoresService scoresService) {
            this.groupCode = groupCode;
            this.privateQQ = privateQQ;
            this.sender = sender;
            this.tag = tag;
            this.num = num;
            this.r18 = r18;
            this.coin = coin;
            this.scoresService = scoresService;
        }

        @Override
        public void run() {
            int sendCount = 0; // 记录实际发送的图片张数
            try {
                List<Pixiv> setu = getSetu(tag, num, r18);
                Pixiv pixiv = setu.get(0);
                String code = pixiv.getCode();
                boolean fromLolicon = "0".equals(code);
                if ("200".equals(code) || fromLolicon || Objects.isNull(code)) {
                    for (Pixiv p : setu) {
                        String filename = p.getFileName();
                        File pic;
                        String imageUrl;
                        if (filename.contains("http")) {
                            imageUrl = filename;
                            pic = new File(TEMP + filename.substring(filename.lastIndexOf("/") + 1));
                        } else {
                            imageUrl = p.getOriginal().replace("pximg.net", "pixiv.cat");
                            pic = new File(TEMP + filename);
                        }
                        InputStream content = Request.Get(imageUrl)
                                .setHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3")
                                .execute().returnResponse().getEntity().getContent();
                        if (!pic.exists()) {
                            FileUtils.copyInputStreamToFile(content, pic);
                        }
                        // 发送图片
                        CQCode cqCodeImage = CQCodeUtil.build().getCQCode_Image(pic.getAbsolutePath());
                        String message = cqCodeImage + "\n" +
                                p.getTitle() + "\n" +
                                ARTWORK_PREFIX + p.getArtwork() + "\n" +
                                p.getAuthor() + "\n" +
                                ARTIST_PREFIX + p.getArtist() + "\n";
                        // + "tags:" + Arrays.toString(p.getTags());
                        if (fromLolicon) {
                            message += "\n" + "今日剩余额度：" + p.getQuota();
                        }
                        if (StringUtils.isEmpty(groupCode)) { // 不是群消息，则直接私聊
                            sender.SENDER.sendPrivateMsg(privateQQ, message);
                        } else {
                            if (!p.isR18()) { // 非R18且叫车的是群消息
                                sender.SENDER.sendGroupMsg(groupCode, message);
                            } else {  // R18则发送私聊
                                sender.SENDER.sendPrivateMsg(privateQQ, message);
                            }
                        }
                        sendCount++;
                    }
                    coin.setScore((int) (coin.getScore() - PRICE * sendCount));
                    scoresService.updateById(coin); // 按照实际发送的张数来扣除叫车者的币
                } else {
                    if (StringUtils.isEmpty(groupCode)) {
                        sender.SENDER.sendPrivateMsg(privateQQ, pixiv.getMsg());
                    } else {
                        sender.SENDER.sendGroupMsg(groupCode, pixiv.getMsg());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                sender.SENDER.sendGroupMsg(groupCode, "炸了");
            }
        }
    }
}
