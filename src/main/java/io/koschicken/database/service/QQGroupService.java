package io.koschicken.database.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.koschicken.database.bean.QQGroup;

import java.util.List;

public interface QQGroupService extends IService<QQGroup> {
    List<QQGroup> findByQQ(String qq);

    QQGroup findOne(String qq, String groupCode);

    void deleteOne(String qq, String groupCode);
}
