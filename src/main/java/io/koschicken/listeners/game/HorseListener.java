package io.koschicken.listeners.game;

import com.forte.qqrobot.anno.Filter;
import com.forte.qqrobot.anno.Listen;
import com.forte.qqrobot.beans.messages.msgget.GroupMsg;
import com.forte.qqrobot.beans.messages.types.MsgGetTypes;
import com.forte.qqrobot.bot.BotManager;
import com.forte.qqrobot.bot.BotSender;
import com.forte.qqrobot.sender.MsgSender;
import io.koschicken.bean.Horse;
import io.koschicken.constants.Constants;
import io.koschicken.constants.GameConstants;
import io.koschicken.database.bean.Scores;
import io.koschicken.database.service.ScoresService;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.koschicken.constants.GameConstants.EMOJI_LIST;

@Component
public class HorseListener {

    //赛马  群号->映射群员->映射押注对象号码 押注金额
    private static final HashMap<String, Map<Long, long[]>> MA_LIST = new HashMap<>();
    private static final HashMap<String, Integer> PROGRESS_LIST = new HashMap<>(); // 赛马进度
    private static final Logger LOGGER = LoggerFactory.getLogger(HorseListener.class);

    @Autowired
    ScoresService scoresService;

    @Autowired
    private BotManager botManager;

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#赛马.*", "#赛.*"}, at = true)
    public void startHorse(GroupMsg msg, MsgSender sender) {
        // 必须开启才可以开始比赛 比赛只能同时开启一次
        if (MA_LIST.get(msg.getGroupCode()) != null) {
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "已经有比赛在进行了");
        } else {
            MA_LIST.put(msg.getGroupCode(), new HashMap<>());
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "比赛开盘");
        }
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = "#开始赛.*", at = true)
    public void start(GroupMsg msg, MsgSender sender) {
        if (MA_LIST.get(msg.getGroupCode()) != null) {
            Integer progress = PROGRESS_LIST.get(msg.getGroupCode());
            if (progress != null && progress >= 0) {
                sender.SENDER.sendGroupMsg(msg.getGroupCode(), "赛马已经开始过了");
            } else {
                sender.SENDER.sendGroupMsg(msg.getGroupCode(), "赛马开始，走过路过不要错过");
                Horse horse = new Horse();
                sender.SENDER.sendGroupMsg(msg.getGroupCode(), drawHorse(horse));
                HorseFight horseFight = new HorseFight(msg.getGroupCode(), horse);
                horseFight.start();
            }
        } else {
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "请先#赛马@机器人");
        }
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"押马[1-5]#[0-9]*"})
    public void buyHorse(GroupMsg msg, MsgSender sender) {
        if (MA_LIST.get(msg.getGroupCode()) == null) {
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "当前没有比赛，不能下注");
            return;
        }
        Integer progress = PROGRESS_LIST.get(msg.getGroupCode());
        if (progress != null && progress > 2) {
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "赛程已经过半，不能再下注了");
            return;
        }
        String re = "^押马([1-5])#([0-9]+)$";
        String str = msg.getMsg();
        Pattern p = Pattern.compile(re);
        Matcher m = p.matcher(str);
        int no = 0;
        int coin = 0;
        while (m.find()) {
            no = Integer.parseInt(m.group(1));
            coin = Integer.parseInt(m.group(2));
        }
        if (no > 5 || no < 1) {
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "没有这个编号的选手");
            return;
        }
        if (coin < 0) {
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "反向下注不可取");
            return;
        }
        Scores byId = scoresService.getById(msg.getCodeNumber());
        if (byId == null || byId.getScore() - coin < 0) {
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "没那么多可以下注的币");
            return;
        }

        if (MA_LIST.get(msg.getGroupCode()).get(msg.getCodeNumber()) != null) {
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "你已经下过注了");
        } else {
            long[] value = new long[2];
            value[0] = no - 1L;
            value[1] = coin;
            MA_LIST.get(msg.getGroupCode()).put(msg.getCodeNumber(), value);
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "下注完成 加油啊" + no + "号");
        }
        int size = MA_LIST.get(msg.getGroupCode()).size();
        if (size > 4) {
            start(msg, sender);
        }
    }

    /**
     * 根据传入的马赛场实况类，制作出马赛场图
     */
    public String drawHorse(Horse horse) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < horse.getPosition().size(); i++) {
            stringBuilder.append(i + 1);
            for (int j = 0; j < 9 - horse.getPosition().get(i); j++) {
                stringBuilder.append("Ξ"); //
            }
            stringBuilder.append(EMOJI_LIST[horse.getType().get(i)]);//画马
            for (int j = 0; j < horse.getPosition().get(i) - 1; j++) {
                stringBuilder.append("Ξ");//
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public void allClear(String groupQQ, int winner) {
        Map<Long, long[]> group = MA_LIST.get(groupQQ);
        Iterator<Long> iterator = group.keySet().iterator();
        List<Scores> list = new ArrayList<>();
        while (iterator.hasNext()) {
            Long entry = iterator.next();
            if (group.get(entry)[0] == winner) {
                Scores scores = scoresService.getById(entry);
                scores.setScore((long) (scores.getScore() + group.get(entry)[1] * 1.5));
                list.add(scores);
            } else {
                Scores byId = scoresService.getById(entry);
                byId.setScore(byId.getScore() - group.get(entry)[1]);
                list.add(byId);
            }
        }
        scoresService.updateBatchById(list);
        MA_LIST.remove(groupQQ);
        PROGRESS_LIST.remove(groupQQ);
    }

    public class HorseFight extends Thread {
        private final String groupQQ;
        private final Horse horse;
        private final List<Integer> horseList;
        private boolean fighting = true;
        private int winnerHorse;

        public HorseFight(String groupQQ, Horse horse) {
            this.groupQQ = groupQQ;
            this.horse = horse;
            horseList = horse.getPosition();
        }

        @Override
        public void run() {
            final BotSender sender = botManager.defaultBot().getSender();
            int progress = 0;
            while (fighting) {
                PROGRESS_LIST.put(groupQQ, progress++);
                try {
                    Thread.sleep(RandomUtils.nextInt(1, 1000) + 2000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
                String s = event();
                sender.SENDER.sendGroupMsg(groupQQ, s);//事件发生器
                add();//所有马向前跑一格
                sender.SENDER.sendGroupMsg(groupQQ, drawHorse(horse));
                try {
                    Thread.sleep(RandomUtils.nextInt(1, 1000) + 3000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
            sender.SENDER.sendGroupMsg(groupQQ, drawHorse(horse));//最后再画一次马图
            sender.SENDER.sendGroupMsg(groupQQ, winnerHorse + 1 + "最终赢得了胜利，让我们为他鼓掌");
            StringBuilder sb = getWinners();
            sender.SENDER.sendGroupMsg(groupQQ, sb.toString());
            allClear(groupQQ, winnerHorse);//收钱
        }

        private StringBuilder getWinners() {
            Map<Long, long[]> map = MA_LIST.get(groupQQ); // int[0]->马的编号 int[1]->钱
            List<Long> winner = new ArrayList<>();
            map.forEach((qq, value) -> {
                long[] intArray = map.get(qq);
                if (Arrays.binarySearch(intArray, winnerHorse) >= 0) {
                    winner.add(qq);
                }
            });
            StringBuilder sb = new StringBuilder();
            if (winner.isEmpty()) {
                sb.append("本次赛马无人押中，很遗憾");
            } else {
                sb.append("恭喜");
                for (Long qq : winner) {
                    sb.append(Constants.CQ_AT).append(qq).append("] ");
                }
                sb.append("赢得了奖金");
            }
            return sb;
        }

        public void add() {
            List<Integer> winners = new ArrayList<>();
            List<Integer> list = horse.getPosition();
            for (int i = 0; i < horse.getPosition().size(); i++) {
                int j = list.get(i) + 1;
                list.set(i, j);
                //一个马跑完全程停止
                if (j > 9) {
                    winners.add(i);
                    fighting = false;
                }
            }
            if (!fighting) {
                // 随机获取一个赢家
                int size = winners.size();
                LOGGER.info("完成比赛的马数量为{}", size);
                if (size > 1) {
                    int i = RandomUtils.nextInt(0, size);
                    LOGGER.info("赢家下标为{}", i);
                    winnerHorse = winners.get(i);
                } else {
                    winnerHorse = winners.get(0);
                }
            }
        }

        public String event() {
            //计算这次发生的是好事还是坏事
            if (RandomUtils.nextInt(1, 77) > 32) {
                //好事
                int i = RandomUtils.nextInt(1, horse.getPosition().size());//作用于哪只马
                horseList.set(i, horseList.get(i) + 1);
                return GameConstants.HORSE_EVENT.getGoodHorseEvent().get(
                        RandomUtils.nextInt(1, GameConstants.HORSE_EVENT.getGoodHorseEvent().size())).replace("?", String.valueOf(i + 1));
            } else {
                //坏事
                int i = RandomUtils.nextInt(1, horse.getPosition().size());
                horseList.set(i, horseList.get(i) - 1);
                return GameConstants.HORSE_EVENT.getBedHorseEvent().get(
                        RandomUtils.nextInt(1, GameConstants.HORSE_EVENT.getBedHorseEvent().size())).replace("?", String.valueOf(i + 1));
            }
        }
    }
}