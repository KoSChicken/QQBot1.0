package io.koschicken.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.koschicken.database.bean.Live;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface LiveMapper extends BaseMapper<Live> {
    @Select("select * from live where qq = #{qq}")
    List<Live> findByQQ(@Param("qq") String qq);

    @Delete("delete from live where qq = #{qq} and bili_uid = #{biliUid}")
    void deleteOne(@Param("qq") String qq, @Param("bili_uid") String biliUid);

    @Select("select g.group_code from live l left join qq_group g on g.qq = l.qq where l.bili_uid = #{biliUid}")
    List<String> findGroupByUid(String biliUid);
}
