package io.koschicken.listeners.intercept;

import com.forte.qqrobot.anno.depend.Beans;
import com.forte.qqrobot.beans.messages.msgget.GroupMsg;
import com.forte.qqrobot.listener.MsgGetContext;
import com.forte.qqrobot.listener.MsgIntercept;
import io.koschicken.bean.GroupPower;
import io.koschicken.constants.Constants;
import io.koschicken.utils.JSONUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Beans
public class PCRIntercept implements MsgIntercept {

    public static Map<String, GroupPower> GROUP_CONFIG_MAP = new ConcurrentHashMap<>(10);

    @Override
    public boolean intercept(MsgGetContext context) {
        if (context.getMsgGet() instanceof GroupMsg) {
            try {
                GroupPower groupPower = GROUP_CONFIG_MAP.get(((GroupMsg) context.getMsgGet()).getGroupCode());
                //总体开关
                if (!groupPower.isGlobalSwitch()) {
                    String masterQQ = Constants.COMMON_CONFIG.getMasterQQ();
                    String qq = ((GroupMsg) context.getMsgGet()).getQQ();
                    return isOpen(context.getMsgGet().getMsg()) && Objects.equals(masterQQ, qq);
                }
                //抽卡消息过滤
                if (isChouKa(context.getMsgGet().getMsg())) {
                    return groupPower.isGachaSwitch();
                }
                //赛马消息过滤
                if (isHorse(context.getMsgGet().getMsg())) {
                    return groupPower.isGachaSwitch();
                }
                //骰子消息过滤
                if (isDice(context.getMsgGet().getMsg())) {
                    return groupPower.isGachaSwitch();
                }
                //setu消息过滤
                if (isSetu(context.getMsgGet().getMsg())) {
                    return groupPower.isGachaSwitch();
                }
            } catch (NullPointerException e) {
                //没这个群的信息
                GroupPower groupPower = new GroupPower();
                groupPower.setGlobalSwitch(Constants.COMMON_CONFIG.isGlobalSwitch());
                groupPower.setMaiyaoSwitch(Constants.COMMON_CONFIG.isMaiyaoSwitch());
                groupPower.setGachaSwitch(Constants.COMMON_CONFIG.isGachaSwitch());
                groupPower.setHorseSwitch(Constants.COMMON_CONFIG.isHorseSwitch());
                groupPower.setHorseSwitch(Constants.COMMON_CONFIG.isDiceSwitch());
                groupPower.setHorseSwitch(Constants.COMMON_CONFIG.isSetuSwitch());
                GROUP_CONFIG_MAP.put(((GroupMsg) context.getMsgGet()).getGroupCode(), groupPower);
                JSONUtils.setJson(GROUP_CONFIG_MAP);
                return Constants.COMMON_CONFIG.isGlobalSwitch();
            }
        }
        return true;
    }

    private boolean isChouKa(String msg) {
        return msg.startsWith("#十连") || msg.startsWith("#up十连") || msg.startsWith("#井")
                || msg.startsWith("#up井") || msg.startsWith("#抽卡") || msg.startsWith("#up抽卡");
    }

    private boolean isHorse(String msg) {
        return msg.startsWith("#赛") || msg.startsWith("#开始赛") || msg.startsWith("押马");
    }

    private boolean isDice(String msg) {
        return msg.startsWith("#骰子") || msg.startsWith("骰子说明") || msg.startsWith("押骰子")
                || msg.startsWith("#投掷骰子") || msg.startsWith("#豹？") || msg.startsWith("#roll");
    }

    private boolean isSetu(String msg) {
        return msg.startsWith("叫车") || msg.startsWith("#抽奖") || msg.startsWith("#mjx")
                || msg.contains("色图") || msg.contains("涩图");
    }

    public boolean isOpen(String msg) {
        return "#启用Bot".equals(msg);
    }
}
