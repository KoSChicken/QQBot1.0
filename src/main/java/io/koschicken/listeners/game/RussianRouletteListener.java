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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Component
public class RussianRouletteListener {

    private static final Map<String, List<Boolean>> gunMap = new HashMap<>();
    private static final Map<String, List<String>> dead = new HashMap<>();
    private static final Map<String, Map<String, Integer>> continuous = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(RussianRouletteListener.class);

    @Autowired
    ScoresServiceImpl scoresService;

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#轮盘说明"})
    public void rouletteHelp(GroupMsg msg, MsgSender sender) {
        sender.SENDER.sendGroupMsg(msg.getGroupCode(), "#轮盘#[1-6] 不输入数字则默认为1 创建游戏\n创建游戏的人会先开枪\n再次输入#轮盘 开枪\n");
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#轮盘.*"})
    public void startRoulette(GroupMsg msg, MsgSender sender) {
        String qq = msg.getQQ();
        String groupCode = msg.getGroupCode();
        List<String> deadList = dead.get(groupCode);
        if (!CollectionUtils.isEmpty(deadList) && deadList.contains(qq)) {
            sender.SENDER.sendGroupMsg(groupCode, Constants.CQ_AT + qq + "] 死人不要参加游戏。");
        } else {
            List<Boolean> bullets = gunMap.get(groupCode);
            deadList = new ArrayList<>();
            if (bullets != null && bullets.contains(true)) {
                LOGGER.info("{}群开枪前：{}", groupCode, printBullets(bullets));
                int bulletCount = countBullets(bullets);
                // 开枪
                shot(sender, qq, groupCode, bullets, bulletCount, deadList);
            } else {
                bullets = new ArrayList<>();
                int bulletCount = load(msg, bullets);
                gunMap.put(groupCode, bullets);
                LOGGER.info("{}群开枪前：{}", groupCode, printBullets(bullets));
                sender.SENDER.sendGroupMsg(groupCode, Constants.CQ_AT + qq + "] 拿起手枪装填了" + bulletCount + "颗子弹，随后向自己的脑袋扣动了扳机。");
                // 开枪
                shot(sender, qq, groupCode, bullets, bulletCount, deadList);
            }
            LOGGER.info("{}群开枪后：{}", groupCode, printBullets(bullets));
        }
    }

    private String printBullets(List<Boolean> list) {
        StringBuilder sb = new StringBuilder();
        list.forEach(b -> sb.append(b).append(" "));
        return sb.toString();
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

    private void shot(MsgSender sender, String qq, String groupCode, List<Boolean> bullets, int bulletCount, List<String> deadList) {
        Boolean bullet = bullets.get(0);
        int random = RandomUtils.nextInt(0, 1001);
        Map<String, Integer> map = continuous.get(groupCode);
        if (CollectionUtils.isEmpty(map)) {
            map = new HashMap<>();
        }
        Integer shotCount = map.get(qq);
        if (shotCount == null) {
            shotCount = 0;
        }
        String message;
        if (Boolean.TRUE.equals(bullet)) {
            if (random < 996) {
                bulletCount--;
                bullets.remove(0);
                message = Constants.CQ_AT + qq + "] 死了，币-" + (shotCount + 1) * 10000;
                map.remove(qq);
                Scores score = scoresService.getById(qq);
                score.setScore(score.getScore() - ((shotCount + 1)  * 10000L) >= 0 ? score.getScore() - ((shotCount + 1)  * 10000L) : 0);
                scoresService.updateById(score);
                if (bulletCount > 0) {
                    deadList.add(qq);
                    dead.put(groupCode, deadList);
                    message = message + "，还有" + bulletCount + "颗子弹。";
                } else {
                    message = message + "，游戏结束。";
                    clear(groupCode);
                }
            } else {
                message = Constants.CQ_AT + qq + "] 手里的枪卡壳了，还有" + bulletCount + "颗子弹。";
                Collections.shuffle(bullets);
            }
        } else {
            bullets.remove(0);
            Scores score = scoresService.getById(qq);
            score.setScore(Math.min(score.getScore() + 10000, Long.MAX_VALUE));
            scoresService.updateById(score);
            shotCount++;
            map.put(qq, shotCount);
            continuous.put(groupCode, map);
            message = Constants.CQ_AT + qq + "] 安然无恙，币+10000，还有" + bulletCount + "颗子弹。";
        }
        sender.SENDER.sendGroupMsg(groupCode, message);
    }

    private void clear(String groupCode) {
        gunMap.remove(groupCode);
        dead.remove(groupCode);
    }
}
