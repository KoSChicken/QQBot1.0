package io.koschicken.listeners;

import com.forte.qqrobot.anno.Filter;
import com.forte.qqrobot.anno.Listen;
import com.forte.qqrobot.beans.messages.msgget.GroupMsg;
import com.forte.qqrobot.beans.messages.msgget.PrivateMsg;
import com.forte.qqrobot.beans.messages.types.MsgGetTypes;
import com.forte.qqrobot.beans.messages.types.PowerType;
import com.forte.qqrobot.beans.types.KeywordMatchType;
import com.forte.qqrobot.sender.MsgSender;
import io.koschicken.InitConfig;
import io.koschicken.bean.GroupPower;
import io.koschicken.database.service.ScoresService;
import io.koschicken.utils.JSONUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

import static io.koschicken.constants.Constants.COMMON_CONFIG;
import static io.koschicken.listeners.intercept.PCRIntercept.GROUP_CONFIG_MAP;

@Service
public class BotSettingListener {

    @Autowired
    private ScoresService scoresService;

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"wei,zaima?"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void check(GroupMsg msg, MsgSender sender) {
        sender.SENDER.sendGroupMsg(msg.getGroupCode(), "buzai,cmn");
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#启用Bot"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void enable(GroupMsg msg, MsgSender sender) {
        String qq = msg.getQQ();
        String groupCode = msg.getGroupCode();
        try {
            if (COMMON_CONFIG.getMasterQQ().equals(qq)) {
                GROUP_CONFIG_MAP.put(groupCode, GROUP_CONFIG_MAP.get(groupCode).allSwitch(true));
                sender.SENDER.sendGroupMsg(groupCode, "已启用");
                JSONUtils.setJson(GROUP_CONFIG_MAP);
            }
        } catch (NullPointerException e) {
            //没这个群的自动都是同意
            GroupPower groupPower = new GroupPower();
            GROUP_CONFIG_MAP.put(groupCode, groupPower.allSwitch(true));
            sender.SENDER.sendGroupMsg(groupCode, "已启用");
            JSONUtils.setJson(GROUP_CONFIG_MAP);
        }
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#禁用Bot"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void disable(GroupMsg msg, MsgSender sender) {
        String qq = msg.getQQ();
        String groupCode = msg.getGroupCode();
        try {
            if (COMMON_CONFIG.getMasterQQ().equals(qq)) {
                GROUP_CONFIG_MAP.put(groupCode, GROUP_CONFIG_MAP.get(groupCode).allSwitch(false));
                sender.SENDER.sendGroupMsg(groupCode, "已禁用");
                JSONUtils.setJson(GROUP_CONFIG_MAP);
            }
        } catch (NullPointerException e) {
            //没这个群的自动都是同意
            GroupPower groupPower = new GroupPower();
            GROUP_CONFIG_MAP.put(groupCode, groupPower);
            sender.SENDER.sendGroupMsg(groupCode, "已禁用");
            JSONUtils.setJson(GROUP_CONFIG_MAP);
        }
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#启用扭蛋"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void enableGacha(GroupMsg msg, MsgSender sender) {
        String qq = msg.getQQ();
        String groupCode = msg.getGroupCode();
        try {
            PowerType powerType = sender.GETTER.getGroupMemberInfo(groupCode, qq).getPowerType();
            if (powerType.isAdmin() || powerType.isOwner() || COMMON_CONFIG.getMasterQQ().equals(qq)) {
                GROUP_CONFIG_MAP.put(groupCode, GROUP_CONFIG_MAP.get(groupCode).setGachaSwitch(true));
                sender.SENDER.sendGroupMsg(groupCode, "已启用扭蛋");
                JSONUtils.setJson(GROUP_CONFIG_MAP);
            }
        } catch (NullPointerException e) {
            //没这个群的自动都是同意
            GroupPower groupPower = new GroupPower();
            GROUP_CONFIG_MAP.put(groupCode, groupPower);
            sender.SENDER.sendGroupMsg(groupCode, "已启用扭蛋");
            JSONUtils.setJson(GROUP_CONFIG_MAP);
        }
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#禁用扭蛋"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void disableGacha(GroupMsg msg, MsgSender sender) {
        String qq = msg.getQQ();
        String groupCode = msg.getGroupCode();
        try {
            PowerType powerType = sender.GETTER.getGroupMemberInfo(groupCode, qq).getPowerType();
            if (powerType.isAdmin() || powerType.isOwner() || COMMON_CONFIG.getMasterQQ().equals(qq)) {
                GROUP_CONFIG_MAP.put(groupCode, GROUP_CONFIG_MAP.get(groupCode).setGachaSwitch(false));
                sender.SENDER.sendGroupMsg(groupCode, "已禁用扭蛋");
                JSONUtils.setJson(GROUP_CONFIG_MAP);
            }
        } catch (NullPointerException e) {
            //没这个群的自动都是同意
            GroupPower groupPower = new GroupPower();
            groupPower.setGachaSwitch(false);
            GROUP_CONFIG_MAP.put(groupCode, groupPower);
            sender.SENDER.sendGroupMsg(groupCode, "已禁用扭蛋");
            JSONUtils.setJson(GROUP_CONFIG_MAP);
        }
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#开启买药小助手"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void enableMaiyao(GroupMsg msg, MsgSender sender) {
        String qq = msg.getQQ();
        String groupCode = msg.getGroupCode();
        try {
            PowerType powerType = sender.GETTER.getGroupMemberInfo(groupCode, qq).getPowerType();
            if (powerType.isAdmin() || powerType.isOwner() || COMMON_CONFIG.getMasterQQ().equals(qq)) {
                GROUP_CONFIG_MAP.put(groupCode, GROUP_CONFIG_MAP.get(groupCode).setMaiyaoSwitch(true));
                sender.SENDER.sendGroupMsg(groupCode, "已开启提醒买药小助手");
                JSONUtils.setJson(GROUP_CONFIG_MAP);
            }
        } catch (NullPointerException e) {
            //没这个群的自动都是同意
            GroupPower groupPower = new GroupPower();
            GROUP_CONFIG_MAP.put(groupCode, groupPower);
            sender.SENDER.sendGroupMsg(groupCode, "已开启提醒买药小助手");
            JSONUtils.setJson(GROUP_CONFIG_MAP);
        }
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#关闭买药小助手"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void disableMaiyao(GroupMsg msg, MsgSender sender) {
        String qq = msg.getQQ();
        String groupCode = msg.getGroupCode();
        try {
            PowerType powerType = sender.GETTER.getGroupMemberInfo(groupCode, qq).getPowerType();
            if (powerType.isAdmin() || powerType.isOwner() || COMMON_CONFIG.getMasterQQ().equals(qq)) {
                GROUP_CONFIG_MAP.put(groupCode, GROUP_CONFIG_MAP.get(groupCode).setMaiyaoSwitch(false));
                sender.SENDER.sendGroupMsg(groupCode, "已关闭提醒买药小助手");
                JSONUtils.setJson(GROUP_CONFIG_MAP);
            }
        } catch (NullPointerException e) {
            //没这个群的自动都是同意
            GroupPower groupPower = new GroupPower();
            groupPower.setMaiyaoSwitch(false);
            GROUP_CONFIG_MAP.put(groupCode, groupPower);
            sender.SENDER.sendGroupMsg(groupCode, "已关闭提醒买药小助手");
            JSONUtils.setJson(GROUP_CONFIG_MAP);
        }
    }

    @Listen(MsgGetTypes.privateMsg)
    @Filter(value = {"重载设置"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void reloadConfig(PrivateMsg msg, MsgSender sender) {
        String qq = msg.getQQ();
        InitConfig.getConfig();
        InitConfig.getGachaConfig();
        InitConfig.getEvent();
        sender.SENDER.sendPrivateMsg(qq, "扭蛋池，马事件已更新,现在设置为：\n"
                + "提醒买药小助手图片名:" + COMMON_CONFIG.getMaiyaoPic()
                + "\n抽卡上限" + COMMON_CONFIG.getGachaLimit()
                + "\n抽卡冷却:" + COMMON_CONFIG.getGachaCooldown()
                + "\n总开关默认：" + COMMON_CONFIG.isGlobalSwitch()
                + "\n好像没啥用的开关默认：" + COMMON_CONFIG.isMaiyaoSwitch()
                + "\n扭蛋开关默认：" + COMMON_CONFIG.isGachaSwitch()
                + "\n赛马开关默认：" + COMMON_CONFIG.isHorseSwitch()
                + "\nr18私聊开关默认：" + COMMON_CONFIG.isR18Private()
                + "\nmasterQQ：" + COMMON_CONFIG.getMasterQQ());
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#查看本群设置"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void groupConfig(GroupMsg msg, MsgSender sender) {
        String groupCode = msg.getGroupCode();
        GroupPower groupPower = GROUP_CONFIG_MAP.get(groupCode);
        sender.SENDER.sendPrivateMsg(msg.getQQ(), "现在设置为：\n"
                + "扭蛋开关:" + groupPower.isGachaSwitch()
                + "\n买药小助手开关" + groupPower.isMaiyaoSwitch()
                + "\n赛马开关" + groupPower.isHorseSwitch()
                + "\n涩图开关" + groupPower.isSetuSwitch()
                + "\n骰子开关：" + groupPower.isDiceSwitch());
    }

    @Listen(MsgGetTypes.privateMsg)
    @Filter(value = {"通用设置"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void config(PrivateMsg msg, MsgSender sender) {
        String qq = msg.getQQ();
        sender.SENDER.sendPrivateMsg(qq, "现在设置为：\n"
                + "提醒买药小助手图片名:" + COMMON_CONFIG.getMaiyaoPic()
                + "\n抽卡上限" + COMMON_CONFIG.getGachaLimit()
                + "\n抽卡冷却:" + COMMON_CONFIG.getGachaCooldown()
                + "\n总开关默认：" + COMMON_CONFIG.isGlobalSwitch()
                + "\n好像没啥用的开关默认：" + COMMON_CONFIG.isMaiyaoSwitch()
                + "\n扭蛋开关默认：" + COMMON_CONFIG.isGachaSwitch()
                + "\n赛马开关默认：" + COMMON_CONFIG.isHorseSwitch()
                + "\nr18私聊开关默认：" + COMMON_CONFIG.isR18Private()
                + "\nB站：" + StringUtils.isEmpty(COMMON_CONFIG.getBilibiliCookie())
                + "\nLoliconAPIKey：" + StringUtils.isEmpty(COMMON_CONFIG.getLoliconApiKey())
                + "\nmasterQQ：" + COMMON_CONFIG.getMasterQQ());
    }

    @Listen(MsgGetTypes.privateMsg)
    @Filter(value = {"刷新签到"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void refreshSign(PrivateMsg msg, MsgSender sender) {
        String qq = msg.getQQ();
        if (qq.equals(COMMON_CONFIG.getMasterQQ())) {
            scoresService.clearSign();
            sender.SENDER.sendPrivateMsg(qq, "已刷新");
        } else {
            sender.SENDER.sendPrivateMsg(qq, "权限不足");
        }
    }

    @Listen(MsgGetTypes.privateMsg)
    @Filter(value = {"刷新roll"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void refreshRoll(PrivateMsg msg, MsgSender sender) {
        String qq = msg.getQQ();
        if (qq.equals(COMMON_CONFIG.getMasterQQ())) {
            scoresService.clearRoll();
            sender.SENDER.sendPrivateMsg(qq, "已刷新");
        } else {
            sender.SENDER.sendPrivateMsg(qq, "权限不足");
        }
    }

    @Listen(MsgGetTypes.privateMsg)
    @Filter(value = {"清理临时文件夹"}, keywordMatchType = KeywordMatchType.TRIM_EQUALS)
    public void clearTemp(PrivateMsg msg, MsgSender sender) throws IOException {
        String qq = msg.getQQ();
        if (qq.equals(COMMON_CONFIG.getMasterQQ())) {
            File gachaFolder = new File("temp/gacha/");
            if (gachaFolder.exists()) {
                FileUtils.deleteDirectory(gachaFolder);
            }
            File bilibiliFolder = new File("temp/bili/");
            if (bilibiliFolder.exists()) {
                FileUtils.deleteDirectory(bilibiliFolder);
            }
            sender.SENDER.sendPrivateMsg(qq, "清理成功");
        } else {
            sender.SENDER.sendPrivateMsg(qq, "权限不足");
        }
    }
}
