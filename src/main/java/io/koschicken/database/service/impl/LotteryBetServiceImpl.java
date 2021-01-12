package io.koschicken.database.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.koschicken.database.bean.LotteryBet;
import io.koschicken.database.dao.LotteryBetMapper;
import io.koschicken.database.service.LotteryBetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LotteryBetServiceImpl extends ServiceImpl<LotteryBetMapper, LotteryBet> implements LotteryBetService {

    @Autowired
    LotteryBetMapper lotteryBetMapper;

    @Override
    public List<LotteryBet> listToday(String groupCode) {
        return lotteryBetMapper.listToday(groupCode);
    }

    @Override
    public List<LotteryBet> lottery(String lottery, String groupCode) {
        return lotteryBetMapper.lottery(lottery, groupCode);
    }

    @Override
    public LotteryBet findByQQAndGroupCode(String qq, String groupCode) {
        return lotteryBetMapper.findByQQAndGroupCode(qq, groupCode);
    }
}
