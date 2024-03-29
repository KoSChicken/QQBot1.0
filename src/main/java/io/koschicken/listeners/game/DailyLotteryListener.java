package io.koschicken.listeners.game;

import com.forte.qqrobot.anno.Filter;
import com.forte.qqrobot.anno.Listen;
import com.forte.qqrobot.beans.messages.msgget.GroupMsg;
import com.forte.qqrobot.beans.messages.types.MsgGetTypes;
import com.forte.qqrobot.beans.types.KeywordMatchType;
import com.forte.qqrobot.sender.MsgSender;
import io.koschicken.database.bean.Lottery;
import io.koschicken.database.bean.LotteryBet;
import io.koschicken.database.service.LotteryBetService;
import io.koschicken.database.service.LotteryService;
import io.koschicken.database.service.QQGroupService;
import io.koschicken.database.service.ScoresService;
import io.koschicken.timer.Daily;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static io.koschicken.constants.Constants.COMMON_CONFIG;

@Component
public class DailyLotteryListener {

    private static final Long DEFAULT_REWARD = 1000000L;
    private static final Map<Long, String> rewardMap = new HashMap<>();

    static {
        rewardMap.put(100000L, "二等奖");
        rewardMap.put(10000L, "三等奖");
        rewardMap.put(1000L, "四等奖");
    }

    @Autowired
    ScoresService scoresService;

    @Autowired
    QQGroupService qqGroupService;

    @Autowired
    LotteryService lotteryService;

    @Autowired
    LotteryBetService lotteryBetService;

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = "#选彩票#.*")
    public void buy(GroupMsg msg, MsgSender sender) {
        String groupCode = msg.getGroupCode();
        String qq = msg.getQQ();
        String str = msg.getMsg().substring(5);
        if (StringUtils.isNotEmpty(str) && str.length() >= 4) {
            String buy = str.substring(0, 4);
            // 检查今日是否已经选过
            LotteryBet check = lotteryBetService.findByQQAndGroupCode(qq, groupCode);
            if (Objects.isNull(check)) {
                // 检查号码有没有被人选过
                List<LotteryBet> lottery = lotteryBetService.lottery(buy, groupCode);
                if (CollectionUtils.isEmpty(lottery)) {
                    LotteryBet lotteryBet = new LotteryBet();
                    lotteryBet.setQq(qq);
                    lotteryBet.setLottery(buy);
                    lotteryBet.setGroupCode(groupCode);
                    lotteryBet.setCreateTime(new Date());
                    lotteryBetService.save(lotteryBet);
                    sender.SENDER.sendGroupMsg(groupCode, "你选择了" + buy);
                } else {
                    sender.SENDER.sendGroupMsg(groupCode, buy + "已经被人选了");
                }
            } else {
                sender.SENDER.sendGroupMsg(groupCode, "你已经选过了");
            }
        } else {
            sender.SENDER.sendGroupMsg(groupCode, "彩票号码为4位数字");
        }
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#生成彩票"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void createLottery(GroupMsg msg, MsgSender sender) {
        String groupCode = msg.getGroupCode();
        String qq = msg.getQQ();
        if (qq.equals(COMMON_CONFIG.getMasterQQ())) {
            Lottery newLottery = new Lottery(); // 新的彩票
            newLottery.setResult(String.valueOf(RandomUtils.nextInt(1000, 10000)));
            newLottery.setCreateTime(new Date());
            newLottery.setGroupCode(groupCode);
            newLottery.setCurrentReward(DEFAULT_REWARD); // 起始奖金
            lotteryService.save(newLottery);
            sender.SENDER.sendGroupMsg(groupCode, "今日彩票已经生成");
        } else {
            sender.SENDER.sendGroupMsg(groupCode, "权限不足");
        }
    }

    /**
     * 奖励机制：数字正确1位奖励1000，2位10000，3位100000，全中则奖励奖池中的所有奖金
     * 开完奖后会从奖池中扣除消耗的奖金，如果扣除之后剩余不足100000则会补齐到100000
     *
     * @param msg
     * @param sender
     */
    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#开奖"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void endLottery(GroupMsg msg, MsgSender sender) {
        String groupCode = msg.getGroupCode();
        String qq = msg.getQQ();
        if (qq.equals(COMMON_CONFIG.getMasterQQ())) {
            Daily daily = new Daily();
            daily.lottery(groupCode, sender);
        } else {
            sender.SENDER.sendGroupMsg(groupCode, "权限不足");
        }
    }
}
