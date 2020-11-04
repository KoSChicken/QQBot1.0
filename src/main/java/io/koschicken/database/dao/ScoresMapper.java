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

    @Update("update scores set sign_flag = false ")
    void changeTeamSum();

    @Select("select sign_flag from scores where QQ = ${qq}")
    Boolean selectSign(@Param("qq") long qq);

    @Select("select live1, live2, live3, QQ from scores where liveON = true")
    List<Scores> selectlive();

    @Update("update scores set liveON = true where QQ = ${qq}")
    int openlive(@Param("qq") long qq);

    @Update("update scores set liveON = false where QQ = ${qq}")
    int closelive(@Param("qq") long qq);

    @Update("update scores set sign_flag = true where QQ = ${qq}")
    int sign(@Param("qq") long qq);

    @Update("update scores set #{size} = 0 where QQ = ${qq}")
    int clear(@Param("qq") long qq, @Param("size") String size);

    @Update("update scores set #{size} = #{mid} where QQ = ${qq}")
    int setLive(@Param("qq") long qq, @Param("size") String size, @Param("mid") long mid);

    @Update("update scores set score = score + 10000")
    void allRich();

    @Update("update scores set score = score / 2 where QQ = ${qq}")
    void financialCrisis(@Param("qq") long qq);

    @Update("update scores set score = score - ${refund} where QQ = ${qq}")
    void refundWu(@Param("qq") long qq, @Param("refund") Integer refund);

    @Select("select QQ from scores where nickname = #{nickname}")
    Long selectQQByNickname(@Param("nickname") String nickname);

    @Select("select * from scores where group_code like #{groupCode} order by score desc limit 10")
    List<Scores> rank(@Param("groupCode") String groupCode);

    @Select("select group_code from scores where live1 = #{mid} or live2 = #{mid} or live3 = #{mid}")
    List<String> groupCodeByMid(@Param("mid") String mid);
}
