package io.koschicken.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.koschicken.database.bean.Scores;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface ScoresMapper extends BaseMapper<Scores> {

    @Update("update scores set sign_flag = false")
    void clearSign();

    @Update("update Scores set roll_count = 3")
    void clearRoll();

    @Select("select sign_flag from scores where qq = #{qq}")
    Boolean selectSign(@Param("qq") String qq);

    @Update("update scores set live_flag = true where qq = #{qq}")
    int openLive(@Param("qq") String qq);

    @Update("update scores set live_flag = false where qq = #{qq}")
    int closeLive(@Param("qq") String qq);

    @Update("update scores set sign_flag = true where qq = #{qq}")
    void sign(@Param("qq") String qq);

    @Update("update scores set score = score + 10000")
    void allRich();

    @Update("update scores set score = score / 2 where qq = #{qq}")
    void financialCrisis(@Param("qq") String qq);

    @Update("update scores set score = score - #{refund} where qq = #{qq}")
    void refundWu(@Param("qq") String qq, @Param("refund") Integer refund);

    @Select("select qq from scores where nickname = #{nickname}")
    Long selectQQByNickname(@Param("nickname") String nickname);

    @Update("update scores set cygames_win = cygames_win + 1 where qq = #{qq}")
    void cygamesWin(@Param("qq") String qq);

    @Select("select * from scores order by cygames_win desc")
    List<Scores> cygamesRank();
}
