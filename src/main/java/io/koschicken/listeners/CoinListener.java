package io.koschicken.listeners;

import com.forte.qqrobot.anno.Filter;
import com.forte.qqrobot.anno.Listen;
import com.forte.qqrobot.beans.messages.msgget.GroupMsg;
import com.forte.qqrobot.beans.messages.result.GroupMemberInfo;
import com.forte.qqrobot.beans.messages.types.MsgGetTypes;
import com.forte.qqrobot.beans.types.KeywordMatchType;
import com.forte.qqrobot.sender.MsgSender;
import io.koschicken.constants.Constants;
import io.koschicken.database.bean.Lucky;
import io.koschicken.database.bean.QQGroup;
import io.koschicken.database.bean.Scores;
import io.koschicken.database.service.LuckyService;
import io.koschicken.database.service.QQGroupService;
import io.koschicken.database.service.ScoresService;
import io.koschicken.utils.Utils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CoinListener {

    public static final long SIGN_SCORE = 5000;
    private static final Logger LOGGER = LoggerFactory.getLogger(CoinListener.class);

    @Autowired
    ScoresService scoresService;

    @Autowired
    QQGroupService qqGroupService;

    @Autowired
    LuckyService luckyService;

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"签到", "簽到"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void sign(GroupMsg msg, MsgSender sender) {
        String qq = msg.getQQ();
        String groupCode = msg.getGroupCode();
        int rank = RandomUtils.nextInt(1, 101);
        int score = getScore(rank);
        Scores scores = scoresService.getById(qq);
        if (scores != null) {
            if (Boolean.TRUE.equals(scores.getSignFlag())) {
                sender.SENDER.sendGroupMsg(groupCode, Constants.CQ_AT + qq + "] 每天只能签到一次");
                return;
            }
            scores.setScore(scores.getScore() + score);
            scores.setSignFlag(true);
            scoresService.updateById(scores);
            QQGroup group = qqGroupService.findOne(qq, groupCode);
            if (Objects.isNull(group)) {
                qqGroupService.save(new QQGroup(qq, groupCode));
            }
            String message;
            if (score > 15) {
                message = Constants.CQ_AT + qq + "] 签到成功，币+" + score + "，现在币:" + scores.getScore();
            } else {
                Lucky entity = new Lucky(msg.getCodeNumber(), new Date(), score);
                luckyService.save(entity);
                message = Constants.CQ_AT + qq + "] 天选之人！币+" + score + "，现在币:" + scores.getScore();
            }
            sender.SENDER.sendGroupMsg(groupCode, message);
        } else {
            scores = new Scores();
            scores.setQq(qq);
            scores.setSignFlag(true);
            scores.setScore(SIGN_SCORE); // 第一次签到的仍然是5000
            scoresService.save(scores);
            qqGroupService.save(new QQGroup(qq, groupCode));
            sender.SENDER.sendGroupMsg(groupCode, Constants.CQ_AT + qq + "] 签到成功，币+" + SIGN_SCORE);
        }
    }

    private int getScore(int rank) {
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
        return score;
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"我有多少钱", "余额"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void myCoin(GroupMsg msg, MsgSender sender) {
        String qq = msg.getQQ();
        Scores scores = scoresService.getById(qq);
        String groupCode = msg.getGroupCode();
        if (scores != null) {
            String isSign = Boolean.TRUE.equals(scores.getSignFlag()) ? "" : "，还没有签到";
            if (scores.getScore() > 0) {
                sender.SENDER.sendGroupMsg(groupCode, Constants.CQ_AT + msg.getQQ() + "] 有" + scores.getScore() + "块钱" + isSign);
            } else {
                sender.SENDER.sendGroupMsg(groupCode, Constants.CQ_AT + msg.getQQ() + "] 你没钱，穷仔" + isSign);
            }
        } else {
            sender.SENDER.sendGroupMsg(groupCode, Constants.CQ_AT + msg.getQQCode() + "] 冇");
        }
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = "#财富榜")
    public void rank(GroupMsg msg, MsgSender sender) {
        StringBuilder sb = new StringBuilder();
        sb.append("群友财富榜\n");
        List<Scores> list = scoresService.rank(msg.getGroupCode());
        for (int i = 0; i < list.size(); i++) {
            GroupMemberInfo info = sender.GETTER.getGroupMemberInfo(msg.getGroupCode(), String.valueOf(list.get(i).getQq()));
            sb.append(i + 1).append(". ").append(Utils.dealCard(info.getCard())).append("\t余额：").append(list.get(i).getScore()).append("\n");
        }
        sender.SENDER.sendGroupMsg(msg.getGroupCode(), sb.toString().trim());
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = "#天选")
    public void luckyList(GroupMsg msg, MsgSender sender) {
        String groupCode = msg.getGroupCode();
        StringBuilder sb = new StringBuilder();
        sb.append("天选之人榜\n");
        List<Lucky> list = luckyService.listByGroupCode(groupCode);
        for (int i = 0; i < (Math.min(list.size(), 10)); i++) {
            Lucky lucky = list.get(i);
            GroupMemberInfo info = sender.GETTER.getGroupMemberInfo(msg.getGroupCode(), String.valueOf(lucky.getQq()));
            sb.append(i + 1).append(". ").append(Utils.dealCard(info.getCard())).append("\t天选次数：").append(lucky.getCount()).append("\n");
        }
        sb.append("等").append(list.size()).append("位群友");
        sender.SENDER.sendGroupMsg(msg.getGroupCode(), sb.toString().trim());
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
            scoresService.financialCrisis(target);
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
            scoresService.refundWu(msg.getQQ(), refund);
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "恭喜你，成功退款，快查查余额吧 :)");
        }
    }
}
