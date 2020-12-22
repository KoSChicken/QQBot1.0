package io.koschicken.listeners.game;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import io.koschicken.database.service.CharactersService;
import io.koschicken.database.service.ScoresService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.koschicken.constants.Constants.CQ_AT;

@Component
public class GuessVoiceListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuessVoiceListener.class);
    private static final String VOICE_FOLDER = "./voice/";
    private static final String CYGAMES_FOLDER = "cygames/";

    // 群号->(群员, 回答)
    private static final Map<String, Map<String, String>> gameMap = new ConcurrentHashMap<>();
    // 群号->答案
    private static final Map<String, String> answerMap = new ConcurrentHashMap<>();
    // 群号->提示
    private static final Map<String, List<String>> hintMap = new ConcurrentHashMap<>();

    @Autowired
    ScoresService scoresService;
    @Autowired
    CharactersService charactersService;
    @Autowired
    private BotManager botManager;

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#cygames-help"})
    public void cygamesHelp(GroupMsg msg, MsgSender sender) {
        sender.SENDER.sendGroupMsg(msg.getGroupCode(),
                "#cygames 创建游戏；\nbot会发送一句语音；\n输入A+名字（如A日和）；\n当有人答对时游戏结束。\n" +
                "输入#cygames-hint，可以获取提示，提示次数限制为3次，第一次提示种族，第二次提示名字长度，最后一次提示所属公会。");
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
    @Filter(value = {"A.*"})
    public void bet(GroupMsg msg, MsgSender sender) {
        String groupCode = msg.getGroupCode();
        if (gameMap.get(groupCode) == null) {
            sender.SENDER.sendGroupMsg(groupCode, "当前没有游戏");
            return;
        }
        String qq = msg.getQQ();
        String str = msg.getMsg();
        String answer = str.substring(1);
        if (gameMap.get(groupCode).get(qq) != null) {
            sender.SENDER.sendGroupMsg(groupCode, "你已经猜过了");
        } else {
            gameMap.get(groupCode).put(qq, answer);
            if (answerMap.get(groupCode).contains(answer)) {
                sender.SENDER.sendGroupMsg(groupCode, announceWinner(groupCode, qq));
            } else {
                sender.SENDER.sendGroupMsg(groupCode, CQ_AT + qq + "] 猜错了，等下一轮游戏吧");
            }
        }
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter("#cygames-hint")
    public void cygamesHint(GroupMsg msg, MsgSender sender) {
        String groupCode = msg.getGroupCode();
        Map<String, String> map = gameMap.get(groupCode);
        if (map != null) {
            List<String> hintList = hintMap.get(groupCode);
            if (hintList != null && !hintList.isEmpty()) {
                sender.SENDER.sendGroupMsg(groupCode, hintList.get(0));
                hintList.remove(0);
            } else {
                sender.SENDER.sendGroupMsg(groupCode, "提示次数已经用完");
            }
        } else {
            sender.SENDER.sendGroupMsg(groupCode, "当前没有游戏");
        }
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter("#cygames-shut")
    public void cygamesShut(GroupMsg msg, MsgSender sender) {
        String groupCode = msg.getGroupCode();
        gameMap.remove(groupCode);
        hintMap.remove(groupCode);
        answerMap.remove(groupCode);
    }

    private String announceWinner(String groupQQ, String qq) {
        scoresService.cygamesWin(qq);
        gameMap.remove(groupQQ);
        hintMap.remove(groupQQ);
        answerMap.remove(groupQQ);
        return "恭喜" + " " + CQ_AT + qq + "] 猜中答案";
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
                String voice = utils.toCq("record", param);
                LOGGER.info(voice);
                sender.SENDER.sendGroupMsg(groupQQ, voice);
                Integer characterCode = Integer.parseInt(audio.split("_")[2].substring(0, 4));
                Characters answer = charactersService.findByCode(characterCode);
                String name = getName(answer);
                answerMap.put(groupQQ, answer.getName());
                LOGGER.info("答案：{}", name);
                List<String> hintList = new ArrayList<>();
                hintList.add("提示1：种族是" + getProfile(answer, "种族"));
                hintList.add("提示2：姓名长度是" + name.length());
                hintList.add("提示3：所属公会是" + getProfile(answer, "公会"));
                hintMap.put(groupQQ, hintList);
            } else {
                sender.SENDER.sendGroupMsg(groupQQ, "缺少声音资源文件");
            }
        }

        private String getProfile(Characters characters, String key) {
            String profile = characters.getProfile();
            if (profile.endsWith(",")) {
                profile = profile.substring(0, profile.length() - 1);
            }
            if (!StringUtils.isEmpty(profile)) {
                JSONObject jsonObject = JSON.parseObject(profile);
                return jsonObject.getString(key);
            } else {
                return "未知";
            }
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
    }
}
