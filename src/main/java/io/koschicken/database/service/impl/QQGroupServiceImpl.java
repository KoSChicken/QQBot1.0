package io.koschicken.database.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.koschicken.database.bean.QQGroup;
import io.koschicken.database.dao.QQGroupMapper;
import io.koschicken.database.service.QQGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QQGroupServiceImpl extends ServiceImpl<QQGroupMapper, QQGroup> implements QQGroupService {

    @Autowired
    QQGroupMapper qqGroupMapper;

    @Override
    public List<QQGroup> findByQQ(String qq) {
        return qqGroupMapper.findByQQ(qq);
    }

    @Override
    public QQGroup findOne(String qq, String groupCode) {
        return qqGroupMapper.findOne(qq, groupCode);
    }

    @Override
    public void deleteOne(String qq, String groupCode) {
        qqGroupMapper.deleteOne(qq, groupCode);
    }
}
