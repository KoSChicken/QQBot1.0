package io.koschicken.database.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.koschicken.database.bean.Scores;
import io.koschicken.database.dao.QQGroupMapper;
import io.koschicken.database.dao.ScoresMapper;
import io.koschicken.database.service.ScoresService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoresServiceImpl extends ServiceImpl<ScoresMapper, Scores> implements ScoresService {

    @Autowired
    ScoresMapper scoresMapper;

    @Autowired
    QQGroupMapper qqGroupMapper;

    @Override
    public void clearSign() {
        scoresMapper.clearSign();
    }

    @Override
    public void clearRoll() {
        scoresMapper.clearRoll();
    }

    @Override
    public Boolean selectSign(String qq) {
        return scoresMapper.selectSign(qq);
    }

    @Override
    public void sign(String qq) {
        scoresMapper.sign(qq);
    }

    @Override
    public int updateLiveOn(String qq, boolean on) {
        if (on) {
            return scoresMapper.openLive(qq);
        } else {
            return scoresMapper.closeLive(qq);
        }
    }

    @Override
    public void allRich() {
        scoresMapper.allRich();
    }

    @Override
    public void financialCrisis(String qq) {
        scoresMapper.financialCrisis(qq);
    }

    @Override
    public void refundWu(String qq, Integer refund) {
        scoresMapper.refundWu(qq, refund);
    }

    @Override
    public Long findQQByNickname(String nickname) {
        return scoresMapper.selectQQByNickname(nickname);
    }

    @Override
    public List<Scores> rank(String groupCode) {
        return qqGroupMapper.rank(groupCode);
    }
}
