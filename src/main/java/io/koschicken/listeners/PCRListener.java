package io.koschicken.listeners;

import com.forte.qqrobot.anno.Filter;
import com.forte.qqrobot.anno.Listen;
import com.forte.qqrobot.beans.messages.msgget.GroupMsg;
import com.forte.qqrobot.beans.messages.result.GroupMemberInfo;
import com.forte.qqrobot.beans.messages.types.MsgGetTypes;
import com.forte.qqrobot.beans.types.KeywordMatchType;
import com.forte.qqrobot.sender.MsgSender;
import com.forte.qqrobot.system.limit.Limit;
import com.simplerobot.modules.utils.KQCodeUtils;
import io.koschicken.bean.Gacha;
import io.koschicken.constants.Constants;
import io.koschicken.utils.GachaImageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import static io.koschicken.constants.Constants.COMMON_CONFIG;
import static io.koschicken.constants.Constants.CQ_AT;
import static io.koschicken.constants.PCRConstants.*;

@Service
public class PCRListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PCRListener.class);
    private static final Random RANDOM = new Random();
    private final KQCodeUtils kqCodeUtils = KQCodeUtils.getInstance();

    @Limit(10)
    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#十连", "#十連"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void gacha10(GroupMsg msg, MsgSender sender) {
        Gacha gacha = doGacha(10);
        sender.SENDER.sendGroupMsg(msg.getGroupCode(), CQ_AT + msg.getQQCode() + "]" + gacha.getData());
        ban(msg, sender, gacha);
    }

    @Limit(10)
    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#up十连", "#up十連"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void gacha10Up(GroupMsg msg, MsgSender sender) {
        Gacha gacha = doUpGacha(10);
        sender.SENDER.sendGroupMsg(msg.getGroupCode(), CQ_AT + msg.getQQCode() + "]" + gacha.getData());
        ban(msg, sender, gacha);
    }

    @Limit(10)
    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = "#井", keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void gacha300(GroupMsg msg, MsgSender sender) {
        Gacha gacha = doGacha(300);
        sender.SENDER.sendGroupMsg(msg.getGroupCode(), CQ_AT + msg.getQQCode() + "]" + gacha.getData());
        ban(msg, sender, gacha);
    }

    @Limit(10)
    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = "#up井", keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void gacha300Up(GroupMsg msg, MsgSender sender) {
        Gacha gacha = doUpGacha(300);
        sender.SENDER.sendGroupMsg(msg.getGroupCode(), CQ_AT + msg.getQQCode() + "]" + gacha.getData());
        ban(msg, sender, gacha);
    }

    @Limit(10)
    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = "#抽卡", keywordMatchType = KeywordMatchType.STARTS_WITH)
    public void gachaSimple(GroupMsg msg, MsgSender sender) {
        String str = msg.getMsg().replaceAll(" +", "");
        try {
            int q = Integer.parseInt(str.substring(3));//获取抽多少次
            //抽卡次数不能为1以下
            if (q < 1) {
                sender.SENDER.sendPrivateMsg(msg.getQQCode(), "抽尼🐴负数呢？");
                return;
            }
            //抽卡次数不能超过设置的最高值and冷却时间到没到
            if (q <= COMMON_CONFIG.getGachaLimit()) {
                Gacha gacha = doGacha(q);
                sender.SENDER.sendGroupMsg(msg.getGroupCode(),
                        CQ_AT + msg.getQQCode() + "]" + gacha.getData());
                //抽卡太欧需要被禁言
                ban(msg, sender, gacha);
            } else {
                sender.SENDER.sendGroupMsg(msg.getGroupCode(), CQ_AT + msg.getQQCode() + "]" + "做个人吧，抽这么多，家里有矿？");
            }
        } catch (NumberFormatException e) {
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "数字解析错误");
        }
    }

    @Limit(10)
    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = "#up抽卡", keywordMatchType = KeywordMatchType.STARTS_WITH)
    public void gachaUpSimple(GroupMsg msg, MsgSender sender) {
        String str = msg.getMsg().replaceAll(" +", "");
        try {
            int q = Integer.parseInt(str.substring(5));
            //抽卡次数不能为1以下
            if (q < 1) {
                sender.SENDER.sendPrivateMsg(msg.getQQCode(), "抽尼🐴负数呢？");
                return;
            }
            if (q <= COMMON_CONFIG.getGachaLimit()) {
                Gacha gacha = doUpGacha(q);
                sender.SENDER.sendGroupMsg(msg.getGroupCode(), CQ_AT + msg.getQQCode() + "]" + gacha.getData());
                ban(msg, sender, gacha);
            } else {
                sender.SENDER.sendGroupMsg(msg.getGroupCode(), CQ_AT + msg.getQQCode() + "]" + "做个人吧，抽这么多，家里有矿？");
            }
        } catch (NumberFormatException e) {
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "数字解析错误");
        }
    }

    private void ban(GroupMsg msg, MsgSender sender, Gacha gacha) {
        GroupMemberInfo groupMemberInfo = sender.GETTER.getGroupMemberInfo(msg.getGroupCode(), sender.bot().getBotCode(), false);
        boolean admin = groupMemberInfo.getPowerType().isAdmin();
        boolean owner = groupMemberInfo.getPowerType().isOwner();
        if (gacha.isBan() && (admin || owner)) {
            Integer ssrCount = gacha.getSsrCount();
            try {
                long pow = (long) Math.pow(10, ssrCount - 1D);
                LOGGER.info("禁言时间： {}", pow);
                sender.SETTER.setGroupBan(msg.getGroupCode(), msg.getQQCode(), pow * 60);
                sender.SENDER.sendGroupMsg(msg.getGroupCode(), "小伙子你很欧啊，奖励禁言大礼包");
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                LOGGER.error("权限不足");
            }
        }
    }

    /**
     * 普通池的概率
     */
    private Gacha doGacha(int num) {
        RANDOM.setSeed(System.currentTimeMillis());
        int r = 0, sr = 0, ssr = 0;//抽出来的三星二星有几个
        for (int i = 0; i < num - num / 10; i++) {
            int j = RANDOM.nextInt(1000);
            if (j > 1000 - PLATINUM_SSR_CHANCE) {
                ssr++;
            } else if (j > PLATINUM_R_CHANCE) {
                sr++;
            } else {
                r++;
            }
        }
        for (int i = 0; i < num / 10; i++) {
            int j = RANDOM.nextInt(1000);
            if (j > 1000 - PLATINUM_SSR_CHANCE) {
                ssr++;
            } else {
                sr++;
            }
        }
        HashMap<String, Integer> map1 = new HashMap<>();
        HashMap<String, Integer> map2 = new HashMap<>();
        HashMap<String, Integer> map3 = new HashMap<>();
        for (int i = 0; i < ssr; i++) {
            int j = RANDOM.nextInt(SSR.length);
            map1.merge(SSR[j], 1, Integer::sum);
        }
        for (int i = 0; i < sr; i++) {
            int j = RANDOM.nextInt(SR.length);
            map2.merge(SR[j], 1, Integer::sum);
        }
        for (int i = 0; i < r; i++) {
            int j = RANDOM.nextInt(R.length);
            map3.merge(R[j], 1, Integer::sum);
        }
        Gacha g = new Gacha();
        g.setSsrCount(ssr);
        g.setData(getGachaString(r, sr, ssr, map1, map2, map3));
        try {
            g.setBan(num / ssr < 20);
        } catch (ArithmeticException e) {
            g.setBan(false);
        }
        return g;
    }

    /**
     * up池的概率
     */
    private Gacha doUpGacha(int num) {
        RANDOM.setSeed(System.currentTimeMillis());
        int r = 0, sr = 0, ssr = 0;//抽出来的三星二星有几个
        //无保底
        for (int i = 0; i < num - num / 10; i++) {
            int j = RANDOM.nextInt(1000);
            if (j > 1000 - SSR_CHANCE) {
                ssr++;
            } else if (j > 1000 - SSR_CHANCE - SR_CHANCE) {
                sr++;
            } else {
                r++;
            }
        }
        //有保底
        for (int i = 0; i < num / 10; i++) {
            int j = RANDOM.nextInt(1000);
            if (j > 1000 - SSR_CHANCE) {
                ssr++;
            } else {
                sr++;
            }
        }
        HashMap<String, Integer> map1 = new HashMap<>();
        HashMap<String, Integer> map2 = new HashMap<>();
        HashMap<String, Integer> map3 = new HashMap<>();
        for (int i = 0; i < ssr; i++) {
            int q = RANDOM.nextInt(SSR_CHANCE);
            if (q < UP_SSR_CHANCE) {
                //抽出来up角色
                map1.merge(SSR_UP[q % SSR_UP.length], 1, Integer::sum);
            } else {
                int j = RANDOM.nextInt(NO_UP_SSR.length);
                map1.merge(NO_UP_SSR[j], 1, Integer::sum);
            }
        }
        for (int i = 0; i < sr; i++) {
            int q = RANDOM.nextInt(SR_CHANCE);
            if (q < UP_SR_CHANCE) {
                //抽出来up角色
                map2.merge(SR_UP[q % SR_UP.length], 1, Integer::sum);
            } else {
                int j = RANDOM.nextInt(NO_UP_SR.length);
                map2.merge(NO_UP_SR[j], 1, Integer::sum);
            }
        }
        for (int i = 0; i < r; i++) {
            int q = RANDOM.nextInt(R_CHANCE);
            if (q < UP_R_CHANCE) {
                //抽出来up角色
                try {
                    map3.merge(R_UP[q % R_UP.length], 1, Integer::sum);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            } else {
                int j = RANDOM.nextInt(NO_UP_R.length);
                map3.merge(NO_UP_R[j], 1, Integer::sum);
            }
        }
        Gacha g = new Gacha();
        g.setSsrCount(ssr);
        g.setData(getGachaString(r, sr, ssr, map1, map2, map3));
        try {
            g.setBan(num / ssr < 20);
        } catch (ArithmeticException e) {
            g.setBan(false);
        }
        return g;
    }

    /**
     * 组织抽卡结果
     */
    private String getGachaString(int r, int sr, int ssr,
                                  HashMap<String, Integer> map1,
                                  HashMap<String, Integer> map2,
                                  HashMap<String, Integer> map3) {
        StringBuilder stringBuilder = new StringBuilder();
        if (ssr != 0) {
            stringBuilder.append("★★★×").append(ssr);
        }
        if (sr != 0) {
            stringBuilder.append("★★×").append(sr);
        }
        if (r != 0) {
            stringBuilder.append("★×").append(r);
        }
        Set<String> set1 = map1.keySet();
        Set<String> set2 = map2.keySet();
        Set<String> set3 = map3.keySet();
        ArrayList<String> list = new ArrayList<>();
        for (String s : set1) {
            int j = map1.get(s);
            for (int i = 0; i < j; i++) {
                list.add(s);
            }
        }
        //人物图片
        int total = r + sr + ssr;
        if (total == 1 || total == 10) { // 仅在单抽或十连的情况下才显示动画
            for (String s : list) {
                File file = new File("./config/gif/" + s + ".gif");
                String image = kqCodeUtils.toCq(Constants.cqType.IMAGE, Constants.cqPrefix.FILE + file.getAbsolutePath());
                stringBuilder.append(image);
            }
        } else {
            try {
                String s = GachaImageUtil.composeImg(list);
                if (null != s) {
                    File file = new File(s);
                    String image = kqCodeUtils.toCq(Constants.cqType.IMAGE, Constants.cqPrefix.FILE + file.getAbsolutePath());
                    stringBuilder.append(image);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (ssr != 0) {
            stringBuilder.append("\n三星：");
            for (String s : set1) {
                stringBuilder.append(s).append("*").append(map1.get(s)).append(",");
            }
        }
        if (sr != 0) {
            stringBuilder.append("\n二星：");
            for (String s : set2) {
                stringBuilder.append(s).append("*").append(map2.get(s)).append(",");
            }
        }
        if (r != 0) {
            stringBuilder.append("\n一星：");
            for (String s : set3) {
                stringBuilder.append(s).append("*").append(map3.get(s)).append(",");
            }
        }
        return stringBuilder.toString();
    }
}
