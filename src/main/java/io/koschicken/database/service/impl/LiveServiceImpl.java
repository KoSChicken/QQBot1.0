package io.koschicken.database.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.koschicken.database.bean.Live;
import io.koschicken.database.dao.LiveMapper;
import io.koschicken.database.service.LiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LiveServiceImpl extends ServiceImpl<LiveMapper, Live> implements LiveService {

    @Autowired
    LiveMapper liveMapper;

    @Override
    public List<Live> findByQQ(String qq) {
        return liveMapper.findByQQ(qq);
    }

    @Override
    public Live findOne(String qq, String biliUid) {
        return liveMapper.findOne(qq, biliUid);
    }

    @Override
    public void deleteOne(String qq, String biliUid) {
        liveMapper.deleteOne(qq, biliUid);
    }

    @Override
    public List<String> findGroupByUid(String biliUid) {
        return liveMapper.findGroupByUid(biliUid);
    }
}
