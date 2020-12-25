package io.koschicken.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.koschicken.database.bean.QQGroup;
import io.koschicken.database.bean.Scores;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface QQGroupMapper extends BaseMapper<QQGroup> {
    @Select("select s.* from scores s " +
            "left join qq_group g on s.qq = g.qq " +
            "where g.`group_code` = #{groupCode} " +
            "order by s.score desc limit 10")
    List<Scores> rank(@Param("groupCode") String groupCode);

    @Select("select * from qq_group where qq = #{qq}")
    List<QQGroup> findByQQ(String qq);

    @Select("select * from qq_group where qq = #{qq} and `group_code` = #{groupCode}")
    QQGroup findOne(String qq, String groupCode);

    @Delete("delete from qq_group where qq = #{qq} and `group_code` = #{groupCode}")
    void deleteOne(String qq, String groupCode);
}
