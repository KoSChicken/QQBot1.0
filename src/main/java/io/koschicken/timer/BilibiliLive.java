package io.koschicken.timer;

import com.forte.qqrobot.bot.BotManager;
import com.forte.qqrobot.bot.BotSender;
import com.simplerobot.modules.utils.KQCodeUtils;
import io.koschicken.bilibili.Live;
import io.koschicken.constants.Constants;
import io.koschicken.database.service.LiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.koschicken.listeners.BilibiliListener.LIVE_HASH_MAP;
import static io.koschicken.listeners.intercept.PCRIntercept.GROUP_CONFIG_MAP;

@Component
@EnableScheduling
public class BilibiliLive {

    private static final Logger LOGGER = LoggerFactory.getLogger(BilibiliLive.class);

    @Autowired
    LiveService liveService;
    @Autowired
    BotManager botManager;

    @Scheduled(cron = "0/30 * * * * ? ")
    public void execute() {
        fetchLive();
        LOGGER.info("当前有无监听的直播间：{}", LIVE_HASH_MAP.isEmpty() ? "无" : "有");
        Set<String> strings = LIVE_HASH_MAP.keySet();
        HashMap<String, Live> live = new HashMap<>();
        Live cache;
        int i;
        for (String s : strings) {
            cache = LIVE_HASH_MAP.get(s);
            i = cache.getLiveStatus();
            try {
                cache.fresh();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
            //刷新前没开播刷新后开播了
            if (i == 0 && cache.getLiveStatus() == 1) {
                live.put(s, cache);
            }
        }
        BotSender msgSender = botManager.defaultBot().getSender();
        List<io.koschicken.database.bean.Live> list = liveService.list();
        Set<String> uidSet = new HashSet<>();
        list.forEach(l -> uidSet.add(l.getBiliUid()));
        StringBuilder stringBuilder = new StringBuilder();
        for (String uid : uidSet) {
            stringBuilder.delete(0, stringBuilder.length());
            String up = "\nUP：";
            String title = "\n标题：";
            String url = "\n链接：";
            Set<String> groupSet = new HashSet<>(liveService.findGroupByUid(uid));
            Live biliLive = live.get(uid);
            if (biliLive != null) {
                stringBuilder.append("开播啦！").append(up).append(biliLive.getUser().getUname())
                        .append(title).append(biliLive.getTitle()).append(url).append(biliLive.getUrl()).append("\n")
                        .append(KQCodeUtils.getInstance().toCq(Constants.cqType.IMAGE,
                                Constants.cqPrefix.FILE + biliLive.getCover().getAbsolutePath()));
            }
            if (stringBuilder.length() > 0) {
                for (String groupCode : groupSet) {
                    if (GROUP_CONFIG_MAP.get(groupCode).isGlobalSwitch()) {
                        msgSender.SENDER.sendGroupMsg(groupCode, stringBuilder.toString());
                    }
                }
            }
        }
    }

    private void fetchLive() {
        List<io.koschicken.database.bean.Live> list = liveService.list();
        if (list.isEmpty()) {
            LIVE_HASH_MAP.clear();
        } else {
            list.forEach(live -> {
                try {
                    String biliUid = live.getBiliUid();
                    Live biliLive = new Live(biliUid);
                    LIVE_HASH_MAP.putIfAbsent(biliUid, biliLive);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
