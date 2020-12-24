package io.koschicken.database.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.koschicken.database.bean.Lottery;
import io.koschicken.database.dao.LotteryMapper;
import io.koschicken.database.service.LotteryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LotteryServiceImpl extends ServiceImpl<LotteryMapper, Lottery> implements LotteryService {

    @Autowired
    LotteryMapper lotteryMapper;

    @Override
    public Lottery today(String groupCode) {
        return lotteryMapper.today(groupCode);
    }
}
