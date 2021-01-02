package io.koschicken.timer;

import com.forte.qqrobot.beans.messages.result.GroupMemberInfo;
import com.forte.qqrobot.bot.BotManager;
import com.forte.qqrobot.bot.BotSender;
import com.forte.qqrobot.sender.MsgSender;
import io.koschicken.database.bean.Lottery;
import io.koschicken.database.bean.LotteryBet;
import io.koschicken.database.bean.QQGroup;
import io.koschicken.database.bean.Scores;
import io.koschicken.database.service.LotteryBetService;
import io.koschicken.database.service.LotteryService;
import io.koschicken.database.service.QQGroupService;
import io.koschicken.database.service.ScoresService;
import io.koschicken.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static io.koschicken.listeners.intercept.PCRIntercept.GROUP_CONFIG_MAP;

/**
 * 每天0点的定时任务，包括重置签到/roll，清理临时文件夹，生成本日彩票等等操作
 */
@Component
@EnableScheduling
public class Daily {

    private static final Logger LOGGER = LoggerFactory.getLogger(Daily.class);

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
    BotManager botManager;

    @Autowired
    LotteryService lotteryService;

    @Autowired
    LotteryBetService lotteryBetService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void execute() throws IOException {
        scoresService.clearSign(); // 重置签到
        scoresService.clearRoll(); // 重置roll
        clearTemp();
        luckyDog();
    }

    private void clearTemp() throws IOException {
        File gachaFolder = new File("temp/gacha/");
        if (gachaFolder.exists()) {
            FileUtils.deleteDirectory(gachaFolder);
        }
        File bilibiliFolder = new File("temp/bili/");
        if (bilibiliFolder.exists()) {
            FileUtils.deleteDirectory(bilibiliFolder);
        }
    }

    private void luckyDog() {
        GROUP_CONFIG_MAP.forEach((code, power) -> {
            List<Scores> list = scoresService.listByGroupCode(code);
            Collections.shuffle(list);
            Scores scores = list.get(0);
            scores.setScore(scores.getScore() + DEFAULT_REWARD);
            scoresService.updateById(scores);
            BotSender sender = botManager.defaultBot().getSender();
            GroupMemberInfo info = sender.GETTER.getGroupMemberInfo(code, scores.getQq());
            sender.SENDER.sendGroupMsg(code, "今日幸运儿是" + Utils.dealCard(info.getCard()) + "，币+" + DEFAULT_REWARD);
        });
    }

    @Scheduled(cron = "0 5 0 * * ?")
    public void dailyLottery() {
        BotSender sender = botManager.defaultBot().getSender();
        GROUP_CONFIG_MAP.forEach((code, power) -> {
            if (GROUP_CONFIG_MAP.get(code).isGlobalSwitch() && GROUP_CONFIG_MAP.get(code).isLotterySwitch()) {
                lottery(code, sender);
            }
        });
    }

    public void lottery(String groupCode, MsgSender sender) {
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
                    stringBuilder.append("本次彩票无人购买");
                } else {
                    Map<String, Long> winnerMap = getWinnerMap(result, lottery, currentReward);
                    if (!winnerMap.isEmpty()) {
                        stringBuilder.append("本次中奖名单\n");
                        currentReward = announceWinner(sender, groupCode, stringBuilder, currentReward, winnerMap);
                    } else {
                        stringBuilder.append("无人中奖，很遗憾，奖金将累计到下次开奖");
                    }
                }
                newLottery.setCurrentReward(currentReward + DEFAULT_REWARD); // 新的奖金为上期累计奖金+起始奖金
                sender.SENDER.sendGroupMsg(groupCode, stringBuilder.toString());
            } else {
                newLottery.setCurrentReward(DEFAULT_REWARD); // 起始奖金
            }
            lotteryService.save(newLottery);
            sender.SENDER.sendGroupMsg(groupCode, "今日彩票已经生成");
            LOGGER.info("今日彩票已经生成");
        }
    }

    @NotNull
    private Long announceWinner(MsgSender sender, String groupCode,
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
                GroupMemberInfo info = sender.GETTER.getGroupMemberInfo(groupCode, winner);
                String rank = winnerReward >= 100000L ? " 一等奖" : " " + rewardMap.get(winnerReward);
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
