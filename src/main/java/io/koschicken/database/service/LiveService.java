package io.koschicken.database.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.koschicken.database.bean.Live;

import java.util.List;

public interface LiveService extends IService<Live> {
    List<Live> findByQQ(String qq);

    Live findOne(String qq, String biliUid);

    void deleteOne(String qq, String biliUid);

    List<String> findGroupByUid(String biliUid);
}
