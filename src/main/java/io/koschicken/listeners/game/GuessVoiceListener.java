package io.koschicken.listeners.game;

import com.forte.qqrobot.anno.Filter;
import com.forte.qqrobot.anno.Listen;
import com.forte.qqrobot.beans.messages.msgget.GroupMsg;
import com.forte.qqrobot.beans.messages.types.MsgGetTypes;
import com.forte.qqrobot.bot.BotManager;
import com.forte.qqrobot.bot.BotSender;
import com.forte.qqrobot.sender.MsgSender;
import com.simplerobot.modules.utils.KQCodeUtils;
import io.koschicken.constants.Constants;
import io.koschicken.database.bean.Characters;
import io.koschicken.database.bean.Scores;
import io.koschicken.database.service.CharactersService;
import io.koschicken.database.service.ScoresService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

import static io.koschicken.constants.Constants.CQ_AT;

@Component
public class GuessVoiceListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuessVoiceListener.class);
    private static final String VOICE_FOLDER = "./voice/";
    private static final String CYGAMES_FOLDER = "cygames/";

    // 群号->映射群员->映射押注内容 押注金额
    private static final HashMap<String, Map<Long, List<String>>> gameMap = new HashMap<>();

    @Autowired
    ScoresService scoresService;
    @Autowired
    CharactersService charactersService;
    @Autowired
    private BotManager botManager;

    @Value("${cygames.delay}")
    private int delay;

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#cygames-help"})
    public void cygamesHelp(GroupMsg msg, MsgSender sender) {
        sender.SENDER.sendGroupMsg(msg.getGroupCode(),
                "#cygames 创建游戏；\nbot会发送一句语音并给出4个选项；\n输入序号#押注金额；\n之后bot会公布答案和答对的群友名单。");
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter("#cygames")
    public void cygames(GroupMsg msg, MsgSender sender) {
        String groupCode = msg.getGroupCode();
        if (gameMap.get(groupCode) != null) {
            sender.SENDER.sendGroupMsg(groupCode, "已经有游戏在进行了");
        } else {
            gameMap.put(groupCode, new HashMap<>());
            Cygames cygames = new Cygames(groupCode);
            cygames.start();
        }
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"[1234]#[0-9]*"})
    public void bet(GroupMsg msg, MsgSender sender) {
        if (gameMap.get(msg.getGroupCode()) == null) {
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "当前没有游戏");
            return;
        }
        String str = msg.getMsg();
        String[] strings = str.split("#");
        String no = strings[0];
        int coin = Integer.parseInt(strings[1]);
        if (coin < 0) {
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "反向投注不可取");
            return;
        }
        Scores scores = scoresService.getById(msg.getCodeNumber());
        if (scores == null || scores.getScore() - coin < 0) {
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "没那么多币");
            return;
        }
        if (gameMap.get(msg.getGroupCode()).get(msg.getCodeNumber()) != null) {
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "你已经猜过了");
        } else {
            List<String> list = new ArrayList<>();
            list.add(no);
            list.add(String.valueOf(coin));
            gameMap.get(msg.getGroupCode()).put(msg.getCodeNumber(), list);
            scores.setScore(scores.getScore() - coin);
            scoresService.updateById(scores);
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "👌");
        }
    }

    public class Cygames extends Thread {
        private final String groupQQ;

        public Cygames(String groupQQ) {
            this.groupQQ = groupQQ;
        }

        @Override
        public void run() {
            final BotSender sender = botManager.defaultBot().getSender();
            String audio = fetchAudioName();
            if (!StringUtils.isEmpty(audio)) {
                String param = Constants.cqPrefix.FILE + VOICE_FOLDER + CYGAMES_FOLDER + audio;
                KQCodeUtils utils = KQCodeUtils.getInstance();
                String voice = utils.toCq("voice", param);
                LOGGER.info(voice);
                sender.SENDER.sendGroupMsg(groupQQ, voice);
                // 获取角色编号和名称，再获取其他三个角色作为备选答案
                Integer characterCode = Integer.parseInt(audio.split("_")[2].substring(0, 4));
                Characters answer = charactersService.findByCode(characterCode);
                List<Characters> options = getOptions();
                options.add(answer);
                Collections.shuffle(options);
                String answerIndex = "";
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < options.size(); i++) {
                    String name = getName(options.get(i));
                    stringBuilder.append(i + 1).append(". ").append(name).append("\n");
                    if (characterCode.equals(options.get(i).getCode())) {
                        answerIndex = String.valueOf(i + 1);
                    }
                }
                sender.SENDER.sendGroupMsg(groupQQ, stringBuilder.append("60秒后揭晓答案。").toString());
                LOGGER.info("答案：{}", getName(answer));
                try {
                    Thread.sleep(delay * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
                sender.SENDER.sendGroupMsg(groupQQ, "答案为：" + answerIndex + ". " + getName(answer));
                StringBuilder sb = getWinners(answerIndex);
                sender.SENDER.sendGroupMsg(groupQQ, sb.toString());
                allClear(answerIndex);
            } else {
                sender.SENDER.sendGroupMsg(groupQQ, "缺少声音资源文件");
            }
        }

        @NotNull
        private List<Characters> getOptions() {
            List<Characters> options = new ArrayList<>();
            List<Characters> all = charactersService.list();
            for (int i = 0; i < 3; i++) {
                Collections.shuffle(all);
                options.add(all.get(0));
            }
            return options;
        }

        @NotNull
        private String getName(Characters characters) {
            return characters.getName().split(",")[0].replace("\"", "").trim();
        }

        private String fetchAudioName() {
            File cygamesFolder = new File(VOICE_FOLDER + CYGAMES_FOLDER);
            if (cygamesFolder.exists()) {
                String[] extensions = {"m4a"};
                List<File> m4aList = (List<File>) FileUtils.listFiles(cygamesFolder, extensions, false);
                int size = m4aList.size();
                int i = RandomUtils.nextInt(0, size);
                File file = m4aList.get(i);
                return file.getName();
            } else {
                return null;
            }
        }

        private StringBuilder getWinners(String answer) {
            Map<Long, List<String>> map = gameMap.get(groupQQ);
            List<Long> winner = new ArrayList<>();
            map.forEach((qq, value) -> {
                List<String> list = map.get(qq);
                String str = list.get(0);
                if (str.equals(answer)) {
                    winner.add(qq);
                }
            });
            StringBuilder sb = new StringBuilder();
            if (winner.isEmpty()) {
                sb.append("本次游戏无人猜中，很遗憾");
            } else {
                sb.append("恭喜");
                for (Long qq : winner) {
                    sb.append(" ").append(CQ_AT).append(qq).append("] ");
                }
                sb.append("猜中答案，赢得了奖金");
            }
            return sb;
        }

        private void allClear(String answer) {
            Map<Long, List<String>> group = gameMap.get(groupQQ);
            Iterator<Long> iterator = group.keySet().iterator();
            List<Scores> list = new ArrayList<>();
            while (iterator.hasNext()) {
                Long entry = iterator.next();
                String s = group.get(entry).get(0);
                if (s.equals(answer)) {
                    Scores byId = scoresService.getById(entry);
                    byId.setScore(byId.getScore() + Integer.parseInt(group.get(entry).get(1)));
                    list.add(byId);
                }
            }
            scoresService.updateBatchById(list);
            gameMap.remove(groupQQ);
        }
    }
}
