package io.koschicken.listeners.game;

import com.forte.qqrobot.anno.Filter;
import com.forte.qqrobot.anno.Listen;
import com.forte.qqrobot.beans.messages.msgget.GroupMsg;
import com.forte.qqrobot.beans.messages.types.MsgGetTypes;
import com.forte.qqrobot.sender.MsgSender;
import com.simplerobot.modules.utils.KQCodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GuessVoiceListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuessVoiceListener.class);

    @Listen(MsgGetTypes.groupMsg)
    @Filter("#cyy")
    public void startHorse(GroupMsg msg, MsgSender sender) {
        String folder = "D:\\Download\\Music\\";
        String audio = "vo_ci_100101_001.m4a";
        KQCodeUtils utils = KQCodeUtils.getInstance();
        String param = "file=" + folder + audio;
        String voice = utils.toCq("record", param);
        LOGGER.info(voice);
        sender.SENDER.sendGroupMsg(msg.getGroupCode(), voice);
    }
}
