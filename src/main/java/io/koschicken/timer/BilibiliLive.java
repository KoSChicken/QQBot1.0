package io.koschicken.timer;

import com.forte.qqrobot.bot.BotManager;
import com.forte.qqrobot.bot.BotSender;
import com.simplerobot.modules.utils.KQCodeUtils;
import io.koschicken.bilibili.Live;
import io.koschicken.constants.Constants;
import io.koschicken.database.bean.Scores;
import io.koschicken.database.service.LiveService;
import io.koschicken.database.service.ScoresService;
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

    private static final HashMap<String, Live> noticed = new HashMap<>();

    @Autowired
    ScoresService scoresService;
    @Autowired
    LiveService liveService;
    @Autowired
    BotManager botManager;

    @Scheduled(cron = "0 */1 * * * ?")
    public void execute() {
        fetchLive();
        LOGGER.info("当前监听的直播间：\n{}", LIVE_HASH_MAP.isEmpty() ? "无" : printMap());
        Set<String> liveKey = LIVE_HASH_MAP.keySet();
        Live live;
        int liveStatus;
        for (String key : liveKey) {
            live = LIVE_HASH_MAP.get(key);
            liveStatus = live.getLiveStatus();
            // 直播状态为直播中，且没有提醒过
            if (liveStatus == 1 && !noticed.containsKey(key)) {
                noticed.putIfAbsent(key, live); // 标记已提醒
                notice(live);
            } else if (liveStatus != 1){
                noticed.remove(key); // 从已提醒中移除
            }
        }
    }

    private void fetchLive() {
        List<io.koschicken.database.bean.Live> list = liveService.list();
        if (list.isEmpty()) {
            LIVE_HASH_MAP.clear();
        } else {
            LIVE_HASH_MAP.clear();
            list.forEach(live -> {
                try {
                    if (liveFlag(live.getQq())) {
                        String biliUid = live.getBiliUid();
                        Live biliLive = new Live(biliUid);
                        LIVE_HASH_MAP.putIfAbsent(biliUid, biliLive);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private boolean liveFlag(String qq) {
        Scores scores = scoresService.getById(qq);
        if (scores == null) {
            return false;
        } else {
            return scores.getLiveFlag();
        }
    }

    private String printMap() {
        StringBuilder sb = new StringBuilder();
        LIVE_HASH_MAP.forEach((k, v) -> sb.append("up：").append(v.getUser().getUname()).append("\t")
                .append("标题：").append(v.getTitle()).append("\t")
                .append("状态：").append(v.getLiveStatus() == 0 ? "未直播" : "直播中").append("\n"));
        return sb.toString();
    }

    private void notice(Live live) {
        BotSender msgSender = botManager.defaultBot().getSender();
        StringBuilder stringBuilder = new StringBuilder();
        String uid = live.getMid();
        String up = "\nUP：";
        String title = "\n标题：";
        String url = "\n链接：";
        Set<String> groupSet = new HashSet<>(liveService.findGroupByUid(uid));
        stringBuilder.append("开播啦！").append(up).append(live.getUser().getUname())
                .append(title).append(live.getTitle()).append(url).append(live.getUrl()).append("\n")
                .append(KQCodeUtils.getInstance().toCq(Constants.cqType.IMAGE,
                        Constants.cqPrefix.FILE + live.getCover().getAbsolutePath()));
        if (stringBuilder.length() > 0) {
            for (String groupCode : groupSet) {
                if (GROUP_CONFIG_MAP.get(groupCode).isGlobalSwitch()) {
                    msgSender.SENDER.sendGroupMsg(groupCode, stringBuilder.toString());
                }
            }
        }
    }
}
