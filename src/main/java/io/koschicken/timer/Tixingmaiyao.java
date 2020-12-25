package io.koschicken.timer;

import com.forte.qqrobot.bot.BotManager;
import com.forte.qqrobot.bot.BotSender;
import com.simplerobot.modules.utils.KQCodeUtils;
import io.koschicken.constants.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Set;

import static io.koschicken.constants.Constants.COMMON_CONFIG;
import static io.koschicken.listeners.intercept.PCRIntercept.GROUP_CONFIG_MAP;

@Component
@EnableScheduling
public class Tixingmaiyao {

    @Autowired
    BotManager botManager;

    @Scheduled(cron = "0 0 0,6,12,18 * * ?")
    public void execute() {
        BotSender msgSender = botManager.defaultBot().getSender();
        try {
            File file = new File("./image/" + COMMON_CONFIG.getMaiyaoPic());
            String str;
            if (file.exists()) {
                KQCodeUtils kqCodeUtils = KQCodeUtils.getInstance();
                str = kqCodeUtils.toCq(Constants.cqType.IMAGE, Constants.cqPrefix.FILE + file.getAbsolutePath());
            } else {
                str = "图片找不到了cnmd";
            }
            Set<String> groupSet = GROUP_CONFIG_MAP.keySet();
            for (String s : groupSet) {
                if (GROUP_CONFIG_MAP.get(s).isGlobalSwitch() && GROUP_CONFIG_MAP.get(s).isMaiyaoSwitch()) {
                    msgSender.SENDER.sendGroupMsg(s, str);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
