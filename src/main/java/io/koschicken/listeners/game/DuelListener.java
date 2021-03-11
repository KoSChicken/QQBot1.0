package io.koschicken.listeners.game;

import com.forte.qqrobot.anno.Filter;
import com.forte.qqrobot.anno.Listen;
import com.forte.qqrobot.beans.messages.msgget.GroupMsg;
import com.forte.qqrobot.beans.messages.result.GroupMemberInfo;
import com.forte.qqrobot.beans.messages.types.MsgGetTypes;
import com.forte.qqrobot.sender.MsgSender;
import com.simplerobot.modules.utils.KQCodeUtils;
import io.koschicken.constants.Constants;
import io.koschicken.database.bean.Characters;
import io.koschicken.database.bean.Scores;
import io.koschicken.database.service.impl.CharactersServiceImpl;
import io.koschicken.database.service.impl.ScoresServiceImpl;
import io.koschicken.utils.Utils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.koschicken.constants.Constants.CQ_AT;

/**
 * 决斗小游戏
 * 命令列表：
 * 1. #summon 花费 pow(2, N) * 10000 马币，召唤一张卡牌，上限为 8。N为当前持有卡牌数。召唤有可能失败。
 * 2. #duel+@x 发起对x的决斗，胜者可随机获得对方一张卡牌。需要等待被决斗者同意或拒绝，如果被决斗者多次拒绝，会影响召唤成功率。
 * 决斗的形式是俄罗斯轮盘，bot会创建一个长度为6的布尔型List，其中1个元素为true（子弹）。然后随机决斗双方中的一人开始开枪，中枪者负。
 * 如果决斗已经开始，其中任意一方超过30秒不开枪则判负。
 * 如果胜者手中持有的卡牌数量超过 8，则败者卡牌会被交还卡池，胜者获得 pow(2, N - 1) * 10000 马币。
 * 3. #sneak+@x 发起对x的决斗，胜者可随机获得对方一张卡牌。类似于决斗，但无需被偷袭者同意，偷袭者需要支付 10000 马币。
 * 偷袭者首先会roll一次点（1-100），如果点数大于90则偷袭成功，否则进入决斗流程。
 * 进入决斗流程后，被偷袭者先开枪，如果被偷袭者没有开枪不会判负，游戏会保留1分钟后结束。
 * 4. #masterof+角色名 查询指定卡牌所属人。返回群名片/昵称。
 * 5. #mycard 查询自己拥有的卡牌。
 */
@Component
public class DuelListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DuelListener.class);
    private static final Map<String, Duel> duelMap = new ConcurrentHashMap<>();

    @Autowired
    ScoresServiceImpl scoresService;
    @Autowired
    CharactersServiceImpl charactersService;

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#duel-help"})
    public void help(GroupMsg msg, MsgSender sender) {
        File file = new File("./image/duel-help.png");
        KQCodeUtils kqCodeUtils = KQCodeUtils.getInstance();
        String img = kqCodeUtils.toCq(Constants.cqType.IMAGE, Constants.cqPrefix.FILE + file.getAbsolutePath());
        sender.SENDER.sendGroupMsg(msg.getGroupCode(), img);
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#summon", "#召唤"})
    public void summon(GroupMsg msg, MsgSender sender) {
        String groupCode = msg.getGroupCode();
        String qq = msg.getQQ();
        List<Characters> characters = charactersService.listByMaster(qq);
        int size = characters.size();
        if (size < 8) {
            Scores scores = scoresService.getById(qq);
            double score = scores.getScore() * 1D;
            double cost = Math.pow(2D, size * 1D) * 10000D;
            if (score >= cost) {
                Characters summon = summon(qq);
                scores.setScore((long) (score - cost));
                scoresService.updateById(scores);
                sender.SENDER.sendGroupMsg(groupCode, CQ_AT + qq + "] 成功召唤了" + getName(summon));
            } else {
                sender.SENDER.sendGroupMsg(groupCode, "余额不足");
            }
        } else {
            sender.SENDER.sendGroupMsg(groupCode, "已达到卡牌持有上限");
        }
    }

    private Characters summon(String qq) {
        List<Characters> characters = charactersService.listAll();
        Collections.shuffle(characters);
        Characters character = characters.get(0);
        charactersService.update(character.getCode(), qq);
        return character;
    }

    private String getName(Characters characters) {
        String name = characters.getName();
        return name.substring(0, name.indexOf(", ")).replace("\"", "").trim();
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#duel(?!-)\\S+", "#决斗\\S+"})
    public void duel(GroupMsg msg, MsgSender sender) throws InterruptedException {
        String groupCode = msg.getGroupCode();
        String qq = msg.getQQ();
        String targetCq = msg2Cq(msg.getMsg());
        String target = msg2QQ(msg.getMsg());
        String check = check(qq, target, sender.bot().getBotCode());
        if (Objects.isNull(check)) {
            Duel duel = new Duel(Arrays.asList(qq, target));
            if (Objects.isNull(duelMap.get(groupCode))) {
                LOGGER.info("创建新的决斗");
                duelMap.put(groupCode, duel);
            } else {
                sender.SENDER.sendGroupMsg(groupCode, "存在未结束的决斗");
            }
            sender.SENDER.sendGroupMsg(groupCode, CQ_AT + qq + "] 对 " + targetCq + " 发起了决斗，请接受/拒绝");
            Thread.sleep(15 * 1000);
            if (duelMap.get(groupCode) != null && !duelMap.get(groupCode).accepted) {
                LOGGER.info("超时无回复");
                duelMap.remove(groupCode);
                sender.SENDER.sendGroupMsg(groupCode, groupMemberInfo(groupCode, target, sender) + "没有回复，决斗取消");
            }
        } else {
            sender.SENDER.sendGroupMsg(groupCode, check);
        }
    }

    private String check(String qq, String target, String bot) {
        if (Objects.equals(target, bot)) {
            return "不能和bot决斗";
        }
        if (Objects.equals(qq, target)) {
            return "不能和自己决斗";
        }
        return null;
    }

    private String groupMemberInfo(String groupCode, String qq, MsgSender sender) {
        GroupMemberInfo info = sender.GETTER.getGroupMemberInfo(groupCode, qq);
        return Utils.dealCard(info.getCard());
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#接受.*", "#拒绝.*"})
    public void duelResponse(GroupMsg msg, MsgSender sender) throws InterruptedException {
        String groupCode = msg.getGroupCode();
        String qq = msg.getQQ();
        Duel duel = duelMap.get(groupCode);
        if (Objects.nonNull(duel)) {
            if (Objects.equals(duel.getPerson().get(1), qq)) {
                if (msg.getMsg().contains("接受") && !duel.accepted) {
                    duel.setAccepted(true);
                    sender.SENDER.sendGroupMsg(groupCode, CQ_AT + duel.getPerson().get(0) + "] 对方接受了决斗，进入下注阶段");
                    Thread.sleep(30 * 1000);
                    duel.setStart(true);
                    sender.SENDER.sendGroupMsg(groupCode, CQ_AT + duel.getShootOrder().get(0) + "] 决斗开始，你先开枪");
                } else if (msg.getMsg().contains("拒绝")) {
                    duelMap.remove(groupCode);
                    sender.SENDER.sendGroupMsg(groupCode, CQ_AT + duel.getPerson().get(0) + "] 对方拒绝与你决斗");
                }
            } else {
                sender.SENDER.sendGroupMsg(groupCode, "无人同你决斗");
            }
        } else {
            sender.SENDER.sendGroupMsg(groupCode, "当前没有任何决斗");
        }
    }

    private String msg2Cq(String message) {
        KQCodeUtils kqCodeUtils = KQCodeUtils.getInstance();
        return kqCodeUtils.getCq(message, 0);
    }

    private String msg2QQ(String message) {
        KQCodeUtils kqCodeUtils = KQCodeUtils.getInstance();
        return kqCodeUtils.getParam(message, "qq");
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#下注[1-2]#[0-9]*"})
    public void bet(GroupMsg msg, MsgSender sender) {
        String groupCode = msg.getGroupCode();
        String qq = msg.getQQ();
        Duel duel = duelMap.get(groupCode);
        if (Objects.nonNull(duel)) {
            if (duel.start) {
                sender.SENDER.sendGroupMsg(groupCode, "决斗已经开始，不再接受下注");
                return;
            }
            if (duel.getPerson().contains(qq)) {
                sender.SENDER.sendGroupMsg(groupCode, "决斗者不得下注");
                return;
            }
            if (checkBet(duel, qq)) {
                String regex = "^#下注([1-2])#([0-9]+)$";
                String str = msg.getMsg();
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(str);
                int no = 0;
                int coin = 0;
                while (m.find()) {
                    no = Integer.parseInt(m.group(1));
                    coin = Integer.parseInt(m.group(2));
                }
                if (no > 2 || no < 1) {
                    sender.SENDER.sendGroupMsg(groupCode, "只能选1或2");
                    return;
                }
                if (coin < 0) {
                    sender.SENDER.sendGroupMsg(groupCode, "反向下注不可取");
                    return;
                }
                Scores scores = scoresService.getById(msg.getCodeNumber());
                if (scores == null || scores.getScore() - coin < 0) {
                    sender.SENDER.sendGroupMsg(groupCode, "没那么多可以下注的币");
                    return;
                }
                Map<String, Map<String, Long>> supporters = duel.getSupporters();
                if (Objects.isNull(supporters)) {
                    supporters = new HashMap<>();
                }
                Map<String, Long> set;
                if (no == 1) {
                    set = supporters.get(duel.getPerson().get(0));
                } else {
                    set = supporters.get(duel.getPerson().get(1));
                }
                if (Objects.isNull(set)) {
                    set = new HashMap<>();
                }
                set.put(qq, (long) coin);
                duel.setSupporters(supporters);
            } else {
                sender.SENDER.sendGroupMsg(groupCode, "已经下过注了");
            }
        }
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#开枪"})
    public void shoot(GroupMsg msg, MsgSender sender) {
        String groupCode = msg.getGroupCode();
        String qq = msg.getQQ();
        Duel duel = duelMap.get(groupCode);
        if (Objects.nonNull(duel) && duel.start) {
            List<String> shootOrder = duel.getShootOrder();
            if (qq.equals(shootOrder.get(0))) {
                List<Boolean> gun = duel.getGun();
                if (gun.get(0)) {
                    String winner = shootOrder.get(1);
                    // 处理下注，处理卡牌
                    String string = dealBet(duel, qq, winner, sender, groupCode);
                    String name = dealDuel(qq, winner);
                    sender.SENDER.sendGroupMsg(groupCode,
                            CQ_AT + qq + "] 死了，" + (StringUtils.hasText(name) ? "输掉了" + name : "但他已经没什么可输的了"));
                    sender.SENDER.sendGroupMsg(groupCode, string);
                    duelMap.remove(groupCode);
                } else {
                    shootOrder.remove(0);
                    gun.remove(0);
                    duel.setShootOrder(shootOrder);
                    duel.setGun(gun);
                    sender.SENDER.sendGroupMsg(groupCode, CQ_AT + shootOrder.get(0) + "] 轮到你了");
                }
            }
        }
    }

    private boolean checkBet(Duel duel, String qq) {
        Map<String, Map<String, Long>> supporters = duel.getSupporters();
        if (CollectionUtils.isEmpty(supporters)) {
            return true;
        }
        Map<String, Long> sup1 = supporters.get(duel.getPerson().get(0));
        Map<String, Long> sup2 = supporters.get(duel.getPerson().get(1));
        return !sup1.containsKey(qq) && !sup2.containsKey(qq);
    }

    private String dealBet(Duel duel, String loser, String winner, MsgSender sender, String groupCode) {
        StringBuilder sb = new StringBuilder();
        Map<String, Map<String, Long>> supporters = duel.getSupporters();
        if (!CollectionUtils.isEmpty(supporters)) {
            Map<String, Long> winners = supporters.get(winner);
            sb.append("下注结果：\n");
            winners.forEach((k, v) -> {
                Scores scores = scoresService.getById(k);
                scores.setScore(scores.getScore() + v);
                sb.append(groupMemberInfo(groupCode, k, sender)).append("：币+").append(v).append("\n");
            });
            Map<String, Long> losers = supporters.get(loser);
            losers.forEach((k, v) -> {
                Scores scores = scoresService.getById(k);
                scores.setScore(scores.getScore() - v);
                sb.append(groupMemberInfo(groupCode, k, sender)).append("：币-").append(v).append("\n");
            });
        } else {
            sb.append("本局无人下注");
        }
        return sb.toString();
    }

    private String dealDuel(String loser, String winner) {
        List<Characters> characters = charactersService.listByMaster(loser);
        if (!CollectionUtils.isEmpty(characters)) {
            Collections.shuffle(characters);
            Characters character = characters.get(0);
            // FIXME 修改表结构，master不再记录在角色表里
            //character.setMaster(winner);
            charactersService.updateById(character);
            return getName(character);
        } else {
            return "";
        }
    }

    @Listen(MsgGetTypes.groupMsg)
    @Filter(value = {"#mycard", "#我的卡牌"})
    public void myCard(GroupMsg msg, MsgSender sender) {
        String qq = msg.getQQ();
        List<Characters> characters = charactersService.listByMaster(qq);
        StringBuilder sb = new StringBuilder();
        sb.append("的卡牌如下：\n");
        for (Characters character : characters) {
            sb.append(getName(character)).append("\n");
        }
        sender.SENDER.sendGroupMsg(msg.getGroupCode(), CQ_AT + qq + "] " + sb);
    }

    static class Duel {
        // 枪
        private List<Boolean> gun;
        // 决斗双方
        private List<String> person;
        // 押注MAP key: 决斗者QQ value: 押注者列表
        private Map<String, Map<String, Long>> supporters;

        private boolean accepted = false;

        private boolean start = false;

        private List<String> shootOrder;

        public Duel(List<String> person) {
            this.person = person;
            List<Boolean> gun = new ArrayList<>();
            gun.add(true);
            for (int i = 0; i < 5; i++) {
                gun.add(false);
            }
            Collections.shuffle(gun);
            this.gun = gun;
            List<Integer> list = Arrays.asList(0, 1, 0, 1, 0, 1, 0, 1, 0);
            List<Integer> order = RandomUtils.nextInt(1, 101) >= 50 ? list.subList(0, 7) : list.subList(1, 8);
            List<String> shootOrder = new ArrayList<>();
            for (Integer i : order) {
                shootOrder.add(person.get(i));
            }
            this.shootOrder = shootOrder;
        }

        public List<String> getPerson() {
            return person;
        }

        public void setPerson(List<String> person) {
            this.person = person;
        }

        public Map<String, Map<String, Long>> getSupporters() {
            return supporters;
        }

        public void setSupporters(Map<String, Map<String, Long>> supporters) {
            this.supporters = supporters;
        }

        public List<Boolean> getGun() {
            return gun;
        }

        public void setGun(List<Boolean> gun) {
            this.gun = gun;
        }

        public boolean isAccepted() {
            return accepted;
        }

        public void setAccepted(boolean accepted) {
            this.accepted = accepted;
        }

        public boolean isStart() {
            return start;
        }

        public void setStart(boolean start) {
            this.start = start;
        }

        public List<String> getShootOrder() {
            return shootOrder;
        }

        public void setShootOrder(List<String> shootOrder) {
            this.shootOrder = shootOrder;
        }

        @Override
        public int hashCode() {
            return Objects.hash(person);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Duel duel = (Duel) o;
            return Objects.equals(person, duel.person);
        }

        @Override
        public String toString() {
            return "Duel{" +
                    "gun=" + gun +
                    ", person=" + person +
                    ", supporters=" + supporters +
                    ", accepted=" + accepted +
                    ", start=" + start +
                    '}';
        }
    }
}
