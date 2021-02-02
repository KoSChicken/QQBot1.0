package io.koschicken.listeners.game;

import com.forte.qqrobot.anno.Filter;
import com.forte.qqrobot.anno.Listen;
import com.forte.qqrobot.beans.messages.msgget.GroupMsg;
import com.forte.qqrobot.beans.messages.result.GroupMemberInfo;
import com.forte.qqrobot.beans.messages.types.MsgGetTypes;
import com.forte.qqrobot.sender.MsgSender;
import io.koschicken.constants.Constants;
import io.koschicken.database.bean.Scores;
import io.koschicken.database.service.impl.ScoresServiceImpl;
import io.koschicken.utils.Utils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class NekoGunListener {

    @Autowired
    ScoresServiceImpl scoresService;

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#nekogun"})
    public void nekoGun(GroupMsg msg, MsgSender sender) {
        String qq = msg.getQQ();
        String groupCode = msg.getGroupCode();
        Scores score = scoresService.getById(qq);
        if (score.getNekogun() > 0) {
            score.setNekogun(score.getNekogun() - 1);
            scoresService.updateById(score);
            String message = Constants.CQ_AT + qq + "] 拿起nekogun开始乱射，";
            List<Scores> list = scoresService.listByGroupCode(groupCode);
            Collections.shuffle(list);
            Scores scores = list.get(0);
            int i = RandomUtils.nextInt(1, 10001);
            long max = Math.max(0, scores.getScore() - i);
            scores.setScore(max);
            scoresService.updateById(scores);
            GroupMemberInfo info = sender.GETTER.getGroupMemberInfo(groupCode, scores.getQq());
            String target = Utils.dealCard(info.getCard()) + "(" + info.getQQ() + ")";
            message += "射中" + target + "造成" + i + "点伤害，" + target + "剩余生命值" + max;
            sender.SENDER.sendGroupMsg(groupCode, message);
        } else {
            sender.SENDER.sendGroupMsg(groupCode, Constants.CQ_AT + qq + "] 没子弹了，发送 #nekogun-r 消耗一万生命值恢复子弹。");
        }
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#nekogun-r"})
    public void nekoGunReload(GroupMsg msg, MsgSender sender) {
        String qq = msg.getQQ();
        String groupCode = msg.getGroupCode();
        Scores score = scoresService.getById(qq);
        Long s = score.getScore();
        if (s > 10000) {
            score.setNekogun(score.getNekogun() + 10);
            score.setScore(s - 10000);
            scoresService.updateById(score);
            String message = Constants.CQ_AT + qq + "] 重新装填了nekogun，剩余生命值：" + (s - 10000);
            sender.SENDER.sendGroupMsg(groupCode, message);
        } else {
            sender.SENDER.sendGroupMsg(groupCode, Constants.CQ_AT + qq + "] 生命值不足");
        }
    }
}
