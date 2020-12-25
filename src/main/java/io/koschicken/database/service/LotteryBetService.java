package io.koschicken.database.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.koschicken.database.bean.LotteryBet;

import java.util.List;

public interface LotteryBetService extends IService<LotteryBet> {

    /**
     * 查询今日所有选择了彩票的群友
     *
     * @param groupCode
     */
    List<LotteryBet> listToday(String groupCode);

    /**
     * 根据彩票号码和群号查找此号码是否可选
     *
     * @param lottery
     * @param groupCode
     */
    List<LotteryBet> lottery(String lottery, String groupCode);

    /**
     * 根据QQ和群号查找*今日*是否购买过
     *
     * @param qq
     * @param groupCode
     */
    LotteryBet findByQQAndGroupCode(String qq, String groupCode);
}
