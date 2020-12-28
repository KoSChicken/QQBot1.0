package io.koschicken.listeners.game;

import com.forte.qqrobot.anno.Filter;
import com.forte.qqrobot.anno.Listen;
import com.forte.qqrobot.beans.messages.msgget.GroupMsg;
import com.forte.qqrobot.beans.messages.result.GroupMemberInfo;
import com.forte.qqrobot.beans.messages.types.MsgGetTypes;
import com.forte.qqrobot.beans.types.KeywordMatchType;
import com.forte.qqrobot.sender.MsgSender;
import io.koschicken.database.bean.Lottery;
import io.koschicken.database.bean.LotteryBet;
import io.koschicken.database.bean.QQGroup;
import io.koschicken.database.bean.Scores;
import io.koschicken.database.service.LotteryBetService;
import io.koschicken.database.service.LotteryService;
import io.koschicken.database.service.QQGroupService;
import io.koschicken.database.service.ScoresService;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static io.koschicken.constants.Constants.COMMON_CONFIG;
import static io.koschicken.listeners.intercept.PCRIntercept.GROUP_CONFIG_MAP;

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
            if (GROUP_CONFIG_MAP.get(groupCode).isGlobalSwitch() && GROUP_CONFIG_MAP.get(groupCode).isLotterySwitch()) {
                // 先生成一张新的彩票但不入库
                Lottery newLottery = new Lottery(); // 新的彩票
                newLottery.setResult(String.valueOf(RandomUtils.nextInt(1000, 10000)));
                newLottery.setCreateTime(new Date());
                newLottery.setGroupCode(groupCode);
                // 查找最新的彩票，如果没有返回则说明从未生成过彩票，将生成一张新的彩票
                Lottery today = lotteryService.today(groupCode);
                if (Objects.nonNull(today)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    String result = today.getResult();
                    stringBuilder.append("彩票开奖，中奖号码为").append(result).append("\n");
                    List<LotteryBet> lottery = lotteryBetService.listToday(groupCode);
                    Long currentReward = today.getCurrentReward();
                    if (lottery.isEmpty()) {
                        stringBuilder.append("无人中奖，很遗憾，奖金将累计到下次开奖");
                        newLottery.setCurrentReward(currentReward + DEFAULT_REWARD); // 新的奖金为上期累计奖金+起始奖金
                    } else {
                        stringBuilder.append("本次中奖名单\n");
                        Map<String, Long> winnerMap = getWinnerMap(result, lottery, currentReward);
                        currentReward = announceWinner(msg, sender, groupCode, stringBuilder, currentReward, winnerMap);
                        newLottery.setCurrentReward(currentReward);
                    }
                    sender.SENDER.sendGroupMsg(groupCode, stringBuilder.toString());
                } else {
                    newLottery.setCurrentReward(DEFAULT_REWARD); // 起始奖金
                }
                lotteryService.save(newLottery);
                sender.SENDER.sendGroupMsg(groupCode, "今日彩票已经生成");
            }
        } else {
            sender.SENDER.sendGroupMsg(groupCode, "权限不足");
        }
    }

    @NotNull
    private Long announceWinner(GroupMsg msg, MsgSender sender, String groupCode,
                                StringBuilder stringBuilder, Long currentReward, Map<String, Long> winnerMap) {
        List<Map.Entry<String, Long>> list = winnerMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());
        Collections.reverse(list);
        int index = 1;
        for (Map.Entry<String, Long> entry : list) {
            String winner = entry.getKey();
            Scores scores = scoresService.getById(winner);
            if (scores == null) {
                scores = createScores(groupCode, winner);
            }
            Long winnerReward = entry.getValue();
            scores.setScore(scores.getScore() + winnerReward);
            currentReward = currentReward - winnerReward;
            if (index <= 10) {
                GroupMemberInfo info = sender.GETTER.getGroupMemberInfo(msg.getGroupCode(), winner);
                String rank = winnerReward > 100000L ? " 一等奖" : " " + rewardMap.get(winnerReward);
                stringBuilder.append(index).append(". ").append(info.getCard()).append(rank).append("，奖金：").append(winnerReward).append("\n");
                index++;
            }
        }
        if (currentReward < DEFAULT_REWARD) {
            currentReward = DEFAULT_REWARD;
        }
        return currentReward;
    }

    @NotNull
    private Map<String, Long> getWinnerMap(String result, List<LotteryBet> lottery, Long currentReward) {
        Map<String, Long> winnerMap = new HashMap<>();
        for (LotteryBet lotteryBet : lottery) {
            char[] arr = lotteryBet.getLottery().toCharArray();
            int count = 0;
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == result.charAt(i)) {
                    count++;
                }
            }
            long reward;
            String winner = lotteryBet.getQq();
            if (count > 0 && count < 4) {
                reward = (long) Math.pow(10, count + 2D);
                winnerMap.put(winner, reward);
            } else if (count == 4) {
                reward = currentReward;
                winnerMap.put(winner, reward);
            }
        }
        return winnerMap;
    }

    @NotNull
    private Scores createScores(String groupCode, String qq) {
        Scores scores;
        scores = new Scores();
        scores.setQq(qq);
        scoresService.save(scores);
        qqGroupService.save(new QQGroup(qq, groupCode));
        return scores;
    }
}
