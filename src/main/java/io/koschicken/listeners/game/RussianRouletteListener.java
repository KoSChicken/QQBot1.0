package io.koschicken.listeners.game;

import com.forte.qqrobot.anno.Filter;
import com.forte.qqrobot.anno.Listen;
import com.forte.qqrobot.beans.messages.msgget.GroupMsg;
import com.forte.qqrobot.beans.messages.types.MsgGetTypes;
import com.forte.qqrobot.sender.MsgSender;
import io.koschicken.constants.Constants;
import io.koschicken.database.bean.Scores;
import io.koschicken.database.service.impl.ScoresServiceImpl;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.koschicken.constants.Constants.COMMON_CONFIG;

@Component
public class RussianRouletteListener {

    private static final Map<String, List<Boolean>> gunMap = new ConcurrentHashMap<>();
    private static final Map<String, List<String>> dead = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Integer>> continuous = new ConcurrentHashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(RussianRouletteListener.class);

    @Autowired
    ScoresServiceImpl scoresService;

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#轮盘说明", "輪盤說明"})
    public void rouletteHelp(GroupMsg msg, MsgSender sender) {
        sender.SENDER.sendGroupMsg(msg.getGroupCode(), "#轮盘#[1-6] 不输入数字则默认为1 创建游戏\n创建游戏的人会先开枪\n再次输入#轮盘 开枪\n");
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#轮盘.*", "#輪盤.*"})
    public void startRoulette(GroupMsg msg, MsgSender sender) {
        String qq = msg.getQQ();
        String groupCode = msg.getGroupCode();
        List<String> deadList = dead.get(groupCode);
        if (!CollectionUtils.isEmpty(deadList)) {
            if (deadList.contains(qq)) {
                sender.SENDER.sendGroupMsg(groupCode, Constants.CQ_AT + qq + "] 死人不要参加游戏。");
            } else {
                shot(msg, sender, qq, groupCode, deadList);
            }
        } else {
            deadList = new CopyOnWriteArrayList<>();
            shot(msg, sender, qq, groupCode, deadList);
        }
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#R", "#r"})
    public void endRoulette(GroupMsg msg, MsgSender sender) {
        String qq = msg.getQQ();
        String groupCode = msg.getGroupCode();
        if (qq.equals(COMMON_CONFIG.getMasterQQ())) {
            clear(groupCode);
            sender.SENDER.sendGroupMsg(groupCode, "💥");
        } else {
            List<Boolean> bullets = gunMap.get(groupCode);
            if (!CollectionUtils.isEmpty(bullets) && countBullets(bullets) == bullets.size()) {
                clear(groupCode);
                sender.SENDER.sendGroupMsg(groupCode, Constants.CQ_AT + qq + "] 扔掉手枪停止了游戏。");
            } else {
                if (bullets == null) {
                    sender.SENDER.sendGroupMsg(groupCode, Constants.CQ_AT + qq + "] 当前没有手枪。");
                } else {
                    sender.SENDER.sendGroupMsg(groupCode, Constants.CQ_AT + qq + "] 游戏没有结束谁都不许下车。");
                }
            }
        }
    }

    private int countBullets(List<Boolean> list) {
        int count = 0;
        for (Boolean hasBullet : list) {
            if (Boolean.TRUE.equals(hasBullet)) {
                count++;
            }
        }
        return count;
    }

    private int load(GroupMsg msg, List<Boolean> bullets) {
        // 装弹
        int bulletCount = 1;
        try {
            bulletCount = Integer.parseInt(msg.getMsg().substring(3));
            if (bulletCount <= 0) {
                bulletCount = 1;
            }
            if (bulletCount > 5) {
                bulletCount = 5;
            }
        } catch (NumberFormatException ignore) {
            // 截取到的字符不是数字，就默认为1
        }
        for (int i = 0; i < 6; i++) {
            bullets.add(i < bulletCount);
        }
        Collections.shuffle(bullets);
        return bulletCount;
    }

    private void shot(GroupMsg msg, MsgSender sender, String qq, String groupCode, List<String> deadList) {
        List<Boolean> bullets = gunMap.get(groupCode);
        if (bullets != null && bullets.contains(true)) {
            int bulletCount = countBullets(bullets);
            // 开枪
            shot(sender, qq, groupCode, bullets, bulletCount, deadList);
        } else {
            bullets = new CopyOnWriteArrayList<>();
            int bulletCount = load(msg, bullets);
            gunMap.put(groupCode, bullets);
            sender.SENDER.sendGroupMsg(groupCode, Constants.CQ_AT + qq + "] 拿起手枪装填了" + bulletCount + "颗子弹，随后向自己的脑袋扣动了扳机。");
            // 开枪
            shot(sender, qq, groupCode, bullets, bulletCount, deadList);
        }
        String bulletStr = StringUtils.collectionToDelimitedString(bullets, ", ");
        LOGGER.info("{}群开枪后：{}", groupCode, bulletStr);
    }

    private void shot(MsgSender sender, String qq, String groupCode, List<Boolean> bullets, int bulletCount, List<String> deadList) {
        Boolean bullet = bullets.get(0);
        int random = RandomUtils.nextInt(0, 1001);
        Map<String, Integer> map = continuous.get(groupCode);
        if (CollectionUtils.isEmpty(map)) {
            map = new ConcurrentHashMap<>();
        }
        int shotCount = Objects.isNull(map.get(qq)) ? 0 : map.get(qq);
        String message;
        Scores score = scoresService.getById(qq);
        if (score == null) {
            sender.SENDER.sendGroupMsg(groupCode, Constants.CQ_AT + qq + "] 先签个到吧。");
            return;
        }
        if (Boolean.TRUE.equals(bullet)) {
            if (random < 986) {
                message = dead(qq, groupCode, bullets, bulletCount, deadList, map, shotCount, score);
            } else if (random < 996) {
                message = Constants.CQ_AT + qq + "] 手里的枪卡壳了，还有" + bulletCount + "颗子弹。";
                Collections.shuffle(bullets);
            } else {
                message = Constants.CQ_AT + qq + "] 手里的枪爆炸了，马币清空。";
                score.setScore(0L);
                scoresService.updateById(score);
            }
        } else {
            message = alive(qq, groupCode, bullets, bulletCount, map, shotCount, score);
        }
        sender.SENDER.sendGroupMsg(groupCode, message);
    }

    @NotNull
    private String dead(String qq, String groupCode, List<Boolean> bullets, int bulletCount, List<String> deadList, Map<String, Integer> map, int shotCount, Scores score) {
        String message;
        bulletCount--;
        bullets.remove(0);
        message = Constants.CQ_AT + qq + "] 死了，币-" + (shotCount + 1) * 10000;
        map.remove(qq);
        score.setScore(score.getScore() - ((shotCount + 1) * 10000L) >= 0 ? score.getScore() - ((shotCount + 1) * 10000L) : 0);
        scoresService.updateById(score);
        deadList.add(qq);
        if (bulletCount > 0) {
            dead.put(groupCode, deadList);
            message = message + "，还有" + bulletCount + "颗子弹。";
        } else {
            message = message + "，游戏结束。";
            clear(groupCode);
        }
        return message;
    }

    @NotNull
    private String alive(String qq, String groupCode, List<Boolean> bullets, int bulletCount, Map<String, Integer> map, int shotCount, Scores score) {
        String message;
        long rate = bullets.size() / (bullets.size() - bulletCount);
        int reward = (int) Math.floor(10000D * rate);
        bullets.remove(0);
        score.setScore(Math.min(score.getScore() + reward, Long.MAX_VALUE));
        scoresService.updateById(score);
        shotCount++;
        map.put(qq, shotCount);
        continuous.put(groupCode, map);
        message = Constants.CQ_AT + qq + "] 安然无恙，币+" + reward + "，还有" + bulletCount + "颗子弹。";
        return message;
    }

    private void clear(String groupCode) {
        gunMap.remove(groupCode);
        dead.remove(groupCode);
        continuous.remove(groupCode);
    }
}
