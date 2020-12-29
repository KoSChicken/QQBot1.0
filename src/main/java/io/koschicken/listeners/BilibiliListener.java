package io.koschicken.listeners;

import com.forte.qqrobot.anno.Filter;
import com.forte.qqrobot.anno.Listen;
import com.forte.qqrobot.beans.messages.msgget.GroupMsg;
import com.forte.qqrobot.beans.messages.msgget.PrivateMsg;
import com.forte.qqrobot.beans.messages.types.MsgGetTypes;
import com.forte.qqrobot.beans.types.KeywordMatchType;
import com.forte.qqrobot.sender.MsgSender;
import com.simplerobot.modules.utils.KQCodeUtils;
import io.koschicken.bilibili.Live;
import io.koschicken.bilibili.User;
import io.koschicken.bilibili.Video;
import io.koschicken.constants.Constants;
import io.koschicken.database.bean.Scores;
import io.koschicken.database.service.LiveService;
import io.koschicken.database.service.ScoresService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BilibiliListener {

    public static final Map<String, Live> LIVE_HASH_MAP = new HashMap<>();

    @Autowired
    LiveService liveService;

    @Autowired
    ScoresService scoresService;

    //查询UP主
    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#UP主", "#up主"}, keywordMatchType = KeywordMatchType.TRIM_STARTS_WITH)
    public void searchUp(GroupMsg msg, MsgSender sender) throws IOException {
        String mid = msg.getMsg().substring(4).trim();
        User user = new User(mid);
        String message = "UP主：" + user.getUname();
        if (user.getRoomId() != 0) {
            message += "\n直播间：" + "https://live.bilibili.com/" + user.getRoomId();
        }
        message += "\n签名:" + user.getSign();

        String image = KQCodeUtils.getInstance()
                .toCq(Constants.cqType.IMAGE, Constants.cqPrefix.FILE + user.getFace().getAbsolutePath());
        message += "\n" + image;
        sender.SENDER.sendGroupMsg(msg.getGroupCode(), message);
    }

    //视频封面 av114514
    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = "#封面", keywordMatchType = KeywordMatchType.TRIM_STARTS_WITH)
    public void videoFace(GroupMsg msg, MsgSender sender) {
        String videoCode = msg.getMsg().substring(3).trim();
        String av = "";
        String bv = "";
        try {
            Video bilibiliVideo = null;
            if (videoCode.toLowerCase().startsWith("av")) {
                bilibiliVideo = new Video(videoCode, false);
                av = videoCode;
                bv = bilibiliVideo.getBv();
            } else if (videoCode.toLowerCase().startsWith("bv")) {
                bilibiliVideo = new Video(videoCode, true);
                av = bilibiliVideo.getAv();
                bv = videoCode;
            }
            if (bilibiliVideo != null) {
                String image = KQCodeUtils.getInstance()
                        .toCq(Constants.cqType.IMAGE, Constants.cqPrefix.FILE + bilibiliVideo.getPic().getAbsolutePath());
                sender.SENDER.sendGroupMsg(msg.getGroupCode(),
                        "av号：" + av + "\nbv号：" + bv + "\n视频标题:" + bilibiliVideo.getTitle() + "\n" + image);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //查询直播状态
    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#直播"}, keywordMatchType = KeywordMatchType.TRIM_STARTS_WITH)
    public void searchLive(GroupMsg msg, MsgSender sender) {
        String mid = msg.getMsg().substring(3).trim();
        Live live = LIVE_HASH_MAP.get(mid);
        if (live == null) {
            try {
                live = new Live(mid);
            } catch (IOException e) {
                if (e.getMessage().contains("412")) {
                    sender.SENDER.sendGroupMsg(msg.getGroupCode(), "Cookie过期");
                } else {
                    sender.SENDER.sendGroupMsg(msg.getGroupCode(), "网络链接错误，请稍后再试");
                }
                return;
            }
        }
        if (live.getRoomStatus() == 0) {
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "该用户还未开通直播间");
            return;
        }
        if (live.getLiveStatus() == 0) {
            if (live.getRoundStatus() == 1) {
                sender.SENDER.sendGroupMsg(msg.getGroupCode(), "在轮播中");
            } else {
                sender.SENDER.sendGroupMsg(msg.getGroupCode(), "还未开播");
            }
        } else {
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "开播啦！\n标题:" + live.getTitle() +
                    "\n链接:" + live.getUrl() + KQCodeUtils.getInstance()
                    .toCq(Constants.cqType.IMAGE, Constants.cqPrefix.FILE + live.getCover().getAbsolutePath()));
        }
    }

    @Listen(MsgGetTypes.privateMsg)
    @Filter(value = {"关注UP"}, keywordMatchType = KeywordMatchType.TRIM_STARTS_WITH)
    public void setLive(PrivateMsg msg, MsgSender sender) {
        Pattern pattern = Pattern.compile("[0-9.]");
        Matcher matcher = pattern.matcher(msg.getMsg());
        StringBuilder stringBuilder = new StringBuilder();
        while (matcher.find()) {
            stringBuilder.append(matcher.group(0));
        }
        String biliUid = stringBuilder.toString();
        String qq = msg.getQQ();
        io.koschicken.database.bean.Live live = liveService.findOne(qq, biliUid);
        if (Objects.isNull(live)) {
            liveService.save(new io.koschicken.database.bean.Live(qq, biliUid));
            sender.SENDER.sendPrivateMsg(msg, "已添加" + stringBuilder + "的开播提示");
            if (liveFlag(qq)) {
                //开始监听直播间
                addLive(biliUid);
            }
        } else {
            sender.SENDER.sendPrivateMsg(msg, "已经关注过" + stringBuilder + "了");
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

    @Listen(MsgGetTypes.privateMsg)
    @Filter(value = {"取关UP"}, keywordMatchType = KeywordMatchType.TRIM_STARTS_WITH)
    public void clearLive(PrivateMsg msg, MsgSender sender) {
        String biliUid = msg.getMsg().substring(4).trim();
        liveService.deleteOne(msg.getQQ(), biliUid);
        sender.SENDER.sendPrivateMsg(msg, "已取关" + biliUid);
    }

    @Listen(MsgGetTypes.privateMsg)
    @Filter(value = {"查看开播提示"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void getLive(PrivateMsg msg, MsgSender sender) {
        String qq = msg.getQQ();
        Scores scores = scoresService.getById(qq);
        if (scores == null) {
            sender.SENDER.sendPrivateMsg(msg, "还没有关注的主播哦");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("开启状态:").append(Boolean.TRUE.equals(scores.getLiveFlag()) ? "开启" : "关闭").append("\n");
            List<io.koschicken.database.bean.Live> liveList = liveService.findByQQ(qq);
            if (!liveList.isEmpty()) {
                sb.append("已关注列表：");
                for (io.koschicken.database.bean.Live live : liveList) {
                    sb.append(live.getBiliUid()).append(", ");
                }
            }
            sender.SENDER.sendPrivateMsg(msg, sb.substring(0, sb.length() - 2));
        }
    }

    @Listen(MsgGetTypes.privateMsg)
    @Filter(value = {"开启开播提示"}, keywordMatchType = KeywordMatchType.TRIM_STARTS_WITH)
    public void openLive(PrivateMsg msg, MsgSender sender) {
        int i = scoresService.updateLiveOn(msg.getQQ(), true);
        if (i < 1) {
            sender.SENDER.sendPrivateMsg(msg, "还没有直播关注记录");
        } else {
            sender.SENDER.sendPrivateMsg(msg, "已开启开播提示功能");
        }
    }

    @Listen(MsgGetTypes.privateMsg)
    @Filter(value = {"关闭开播提示"}, keywordMatchType = KeywordMatchType.TRIM_STARTS_WITH)
    public void closeLive(PrivateMsg msg, MsgSender sender) {
        int i = scoresService.updateLiveOn(msg.getQQ(), false);
        if (i < 1) {
            sender.SENDER.sendPrivateMsg(msg, "还没有直播关注记录");
        } else {
            sender.SENDER.sendPrivateMsg(msg, "已关闭开播提示功能");
        }
    }

    private void addLive(String mid) {
        new AddLive(mid).start();
    }

    static class AddLive extends Thread {
        private final String mid;

        public AddLive(String mid) {
            this.mid = mid;
        }

        @Override
        public void run() {
            if (LIVE_HASH_MAP.get(mid) == null) {
                //没有则加入一个
                boolean flag = true;
                do {
                    try {
                        Live live = new Live(mid);
                        LIVE_HASH_MAP.put(mid, live);
                        flag = false;
                    } catch (IOException e) {
                        //出现了问题则等一会再加上去
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                            Thread.currentThread().interrupt();
                        }
                    }
                } while (flag);
            }
        }
    }
}
