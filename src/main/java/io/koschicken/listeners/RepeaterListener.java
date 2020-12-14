package io.koschicken.listeners;

import com.forte.qqrobot.anno.Filter;
import com.forte.qqrobot.anno.Listen;
import com.forte.qqrobot.beans.messages.msgget.GroupMsg;
import com.forte.qqrobot.beans.messages.types.MsgGetTypes;
import com.forte.qqrobot.sender.MsgSender;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RepeaterListener {

    Map<String, Deque<String>> stackMap = new HashMap<>(); // 用于存储不同群的消息栈

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = ".*")
    public void startRepeat(GroupMsg msg, MsgSender sender) {
        Deque<String> stack = stackMap.get(msg.getGroupCode()); // 根据群号获取消息栈
        String currentMsg = msg.getMsg(); // 获取当前消息内容
        if (stack == null) {
            stack = new ArrayDeque<>();
            stackMap.put(msg.getGroupCode(), stack);
        }
        stackMsg(msg, sender, stack, currentMsg);
    }

    private void stackMsg(GroupMsg msg, MsgSender sender, Deque<String> stack, String currentMsg) {
        if (repeatFlag(currentMsg)) {
            return;
        }
        if (stack.isEmpty()) { // 如果栈是空的，入栈
            stack.push(currentMsg);
        } else {
            if (currentMsg.equals(stack.peek())) { // 如果当前消息内容和栈顶内容相同则入栈
                stack.push(currentMsg);
            } else {
                stack.clear(); // 如果当前消息和栈顶内容不同（复读中断），则清空栈
            }
        }
        if (stack.size() > 3) {
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), currentMsg); // 复读
            stack.clear(); // 复读之后清空栈
        }
    }

    private boolean repeatFlag(String msg) {
        List<String> commandList = new ArrayList<>();
        commandList.add("签到");
        commandList.add("骰子说明");
        commandList.add("叫车");
        commandList.add("余额");
        commandList.add("我有多少钱");
        boolean containsSharp = msg.contains("#");
        return containsSharp || commandList.contains(msg);
    }
}
