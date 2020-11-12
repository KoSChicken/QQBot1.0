package io.koschicken.listeners;

import com.forte.qqrobot.anno.Filter;
import com.forte.qqrobot.anno.Listen;
import com.forte.qqrobot.beans.messages.msgget.GroupMsg;
import com.forte.qqrobot.beans.messages.types.MsgGetTypes;
import com.forte.qqrobot.beans.types.KeywordMatchType;
import com.forte.qqrobot.bot.BotManager;
import com.forte.qqrobot.sender.MsgSender;
import io.koschicken.constants.Constants;
import io.koschicken.database.bean.Scores;
import io.koschicken.database.service.ScoresService;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CoinListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoinListener.class);
    public static final int SIGN_SCORE = 5000;

    @Autowired
    ScoresService scoresService;

    @Autowired
    private BotManager botManager;

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"签到", "#签到", "簽到", "#簽到"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void sign(GroupMsg msg, MsgSender sender) {
        int rank = RandomUtils.nextInt(1, 101);
        int score;
        if (rank >= 99) {
            score = RandomUtils.nextInt(25000, 50001);
        } else if (rank >= 95) {
            score = RandomUtils.nextInt(10000, 25001);
        } else if (rank >= 50) {
            score = RandomUtils.nextInt(5000, 10001);
        } else if (rank >= 15) {
            score = RandomUtils.nextInt(2500, 5001);
        } else {
            score = rank;
        }
        Scores scores = scoresService.getById(msg.getCodeNumber());
        if (scores != null) {
            if (Boolean.TRUE.equals(scores.getSignFlag())) {
                sender.SENDER.sendGroupMsg(msg.getGroupCode(), Constants.CQ_AT + msg.getQQ() + "] 每天只能签到一次");
                return;
            }
            scores.setScore(scores.getScore() + score);
            scores.setSignFlag(true);
            String groupCode = scores.getGroupCode();
            if (groupCode != null && !groupCode.contains(msg.getGroupCode())) {
                scores.setGroupCode(groupCode + ", " + msg.getGroupCode());
            } else {
                scores.setGroupCode(msg.getGroupCode());
            }
            scoresService.updateById(scores);
            if (score > 15) {
                sender.SENDER.sendGroupMsg(msg.getGroupCode(), Constants.CQ_AT + msg.getQQ() + "] 签到成功，币+" + score + "，现在币:" + scores.getScore());
            } else {
                sender.SENDER.sendGroupMsg(msg.getGroupCode(), Constants.CQ_AT + msg.getQQ() + "] 天选之人！币+" + score + "，现在币:" + scores.getScore());
            }
        } else {
            scores = new Scores();
            scores.setQq(msg.getCodeNumber());
            scores.setSignFlag(true);
            scores.setScore(SIGN_SCORE); // 第一次签到的仍然是5000
            scores.setGroupCode(msg.getGroupCode());
            scoresService.save(scores);
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), Constants.CQ_AT + msg.getQQ() + "] 签到成功，币+" + score);
        }
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = "#天降福利")
    public void allRich(GroupMsg msg, MsgSender sender) {
        if (Constants.COMMON_CONFIG.getMasterQQ().equals(msg.getQQ())) {
            scoresService.allRich();
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "所有人的钱包都增加了一万块钱");
        }
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = "#金融危机.*")
    public void financialCrisis(GroupMsg msg, MsgSender sender) {
        if (Constants.COMMON_CONFIG.getMasterQQ().equals(msg.getQQ())) {
            String target;
            String[] split = msg.getMsg().split(" +");
            if (split.length > 1) {
                target = split[1];
            } else {
                sender.SENDER.sendGroupMsg(msg.getGroupCode(), "没有金融危机袭击的目标");
                return;
            }
            scoresService.financialCrisis(Long.parseLong(target));
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), Constants.CQ_AT + target + "] 遭遇金融危机，财产减半。");
        }
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = "退款(.*)")
    public void refundWu(GroupMsg msg, MsgSender sender) {
        String message = msg.getMsg();
        String regex = "退款(.*)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(message);
        int refund = 0;
        while (m.find()) {
            try {
                refund = Integer.parseInt(m.group(1));
            } catch (NumberFormatException e) {
                LOGGER.info("数量不是数字，默认为0");
            }
        }
        if (refund != 0) {
            if (refund < 0) {
                sender.SENDER.sendGroupMsg(msg.getGroupCode(), "退款数值不能为负数");
                return;
            }
            scoresService.refundWu(Long.parseLong(msg.getQQCode()), refund);
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "恭喜你，成功退款，快查查余额吧 :)");
        }
    }
}
