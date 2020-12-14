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
                //总体开关
                if (!(GROUP_CONFIG_MAP.get(((GroupMsg) context.getMsgGet()).getGroupCode())).isGlobalSwitch()) {
                    String masterQQ = Constants.COMMON_CONFIG.getMasterQQ();
                    String qq = ((GroupMsg) context.getMsgGet()).getQQ();
                    return isOpen(context.getMsgGet().getMsg()) && Objects.equals(masterQQ, qq);
                }
                //抽卡消息过滤
                if (isChouKa(context.getMsgGet().getMsg())) {
                    return GROUP_CONFIG_MAP.get(((GroupMsg) context.getMsgGet()).getGroupCode()).isGachaSwitch();
                }
            } catch (NullPointerException e) {
                //没这个群的信息
                GroupPower groupPower = new GroupPower();
                groupPower.setGlobalSwitch(Constants.COMMON_CONFIG.isGlobalSwitch());
                groupPower.setMaiyaoSwitch(Constants.COMMON_CONFIG.isMaiyaoSwitch());
                groupPower.setGachaSwitch(Constants.COMMON_CONFIG.isGachaSwitch());
                groupPower.setHorseSwitch(Constants.COMMON_CONFIG.isHorseSwitch());
                GROUP_CONFIG_MAP.put(((GroupMsg) context.getMsgGet()).getGroupCode(), groupPower);
                JSONUtils.setJson(GROUP_CONFIG_MAP);
                return Constants.COMMON_CONFIG.isGlobalSwitch();
            }
        }
        return true;
    }

    //是抽卡消息吗
    private boolean isChouKa(String msg) {
        return msg.startsWith("#十连") || msg.startsWith("#up十连") || msg.startsWith("#井")
                || msg.startsWith("#up井") || msg.startsWith("#抽卡") || msg.startsWith("#up抽卡");
    }

    public boolean isOpen(String msg) {
        return "#启用Bot".equals(msg);
    }
}