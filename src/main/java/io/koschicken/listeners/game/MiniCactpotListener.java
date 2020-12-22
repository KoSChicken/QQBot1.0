package io.koschicken.listeners.game;

import com.forte.qqrobot.anno.Filter;
import com.forte.qqrobot.anno.Listen;
import com.forte.qqrobot.beans.messages.msgget.GroupMsg;
import com.forte.qqrobot.beans.messages.types.MsgGetTypes;
import com.forte.qqrobot.bot.BotManager;
import com.forte.qqrobot.bot.BotSender;
import com.forte.qqrobot.sender.MsgSender;
import io.koschicken.database.service.impl.ScoresServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 一个写了一半放弃了的FF14仙人彩
 * 因为交互方式不适合群聊
 * 0 1 2
 * 3 4 5
 * 6 7 8
 */
@Component
public class MiniCactpotListener {

    // 群号->映射群员->映射押注内容 押注金额
    private static final Map<String, Map<String, List<String>>> gameMap = new HashMap<>();
    private static final Map<String, List<Integer>> options = new HashMap<>();
    private static final List<Integer> rewards = List.of(36, 720, 360, 80, 252, 108, 72, 54, 180, 72, 180, 119, 36, 306, 1080, 144, 1800, 3600);
    private static final Map<String, Boolean> progressMap = new HashMap<>(); // 游戏状态

    // 初始化选项表 配置了常见的表述作为key，游戏数组的下标作为value
    static {
        List<Integer> r1 = List.of(0, 1, 2);
        options.put("第一行", r1);
        options.put("第1行", r1);
        options.put("r1", r1);

        List<Integer> r2 = List.of(3, 4, 5);
        options.put("第二行", r2);
        options.put("第2行", r2);
        options.put("r2", r2);

        List<Integer> r3 = List.of(6, 7, 8);
        options.put("第三行", r3);
        options.put("第3行", r3);
        options.put("r3", r3);

        List<Integer> c1 = List.of(0, 3, 6);
        options.put("第一列", c1);
        options.put("第1列", c1);
        options.put("c1", c1);

        List<Integer> c2 = List.of(1, 4, 7);
        options.put("第二列", c2);
        options.put("第2列", c2);
        options.put("c2", c2);

        List<Integer> c3 = List.of(2, 5, 8);
        options.put("第三列", c3);
        options.put("第3列", c3);
        options.put("c3", c3);

        List<Integer> ld = List.of(0, 4, 8);
        options.put("左对角", ld);
        options.put("ld", ld);

        List<Integer> rd = List.of(2, 4, 6);
        options.put("右对角", rd);
        options.put("rd", rd);
    }

    @Autowired
    ScoresServiceImpl scoresService;

    @Autowired
    private BotManager botManager;

    @Value("${cactpot.rate}")
    private Integer cactpotRate;

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#cactpot-help"})
    public void diceHelp(GroupMsg msg, MsgSender sender) {
        sender.SENDER.sendGroupMsg(msg.getGroupCode(),
                "#cactpot 创建游戏，bot会初始化一个包含随机分配1~9的九宫格，其中会有一个展示的数字；\n" +
                        "#cactpot-show.x 翻开未展示的九宫格，x代表左上至右下标号为0~8，玩家可以翻开3个格子；\n" +
                        "#cactpot-bet.X 选择竞猜结果，X支持的表述有：“第一/1/二/2/三/3行”，“第一/1/二/2/三/3列”，“左对角”，“右对角”" +
                        "也可以用r1r2r3表示行，c1c2c3表示列，ld表示左对角，rd表示右对角。");
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = "#cactpot")
    public void startCactpot(GroupMsg msg, MsgSender sender) {
        //只能同时开启一次
        if (gameMap.get(msg.getGroupCode()) != null) {
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "已经有游戏在进行了");
        } else {
            gameMap.put(msg.getGroupCode(), new HashMap<>());
            sender.SENDER.sendGroupMsg(msg.getGroupCode(), "游戏开始");
        }
    }

    class Cactpot extends Thread {
        private final String groupQQ;

        public Cactpot(String groupQQ) {
            this.groupQQ = groupQQ;
        }

        @Override
        public void run() {
            final BotSender sender = botManager.defaultBot().getSender();
            // 初始化九宫格
            List<Integer> game = Arrays.asList(1,2,3,4,5,6,7,8,9);
            Collections.shuffle(game);
            progressMap.put(groupQQ, true);

        }
    }
}
