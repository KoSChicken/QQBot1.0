package io.koschicken.database.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.koschicken.database.bean.Scores;

import java.util.List;

public interface ScoresService extends IService<Scores> {

    void clearSign();

    void clearRoll();

    Boolean selectSign(String qq);

    void sign(String qq);

    int updateLiveOn(String qq, boolean on);

    void allRich();

    void financialCrisis(String qq);

    void refundWu(String qq, Integer refund);

    Long findQQByNickname(String nickname);

    List<Scores> rank(String groupCode);

    void cygamesWin(String qq);

    List<Scores> cygamesRank();
}
