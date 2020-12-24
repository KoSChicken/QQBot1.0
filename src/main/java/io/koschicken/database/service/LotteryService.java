package io.koschicken.database.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.koschicken.database.bean.Lottery;

public interface LotteryService extends IService<Lottery> {

    Lottery today(String groupCode);
}
