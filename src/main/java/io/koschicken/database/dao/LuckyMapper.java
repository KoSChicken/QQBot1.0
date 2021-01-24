package io.koschicken.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.koschicken.database.bean.Lucky;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface LuckyMapper extends BaseMapper<Lucky> {
    @Select("select l.qq, count(l.qq) as 'count' " +
            "from lucky l left join qq_group g on g.qq = l.qq " +
            "where g.group_code = #{groupCode} " +
            "group by l.qq order by count(l.qq) desc")
    List<Lucky> list(String groupCode);

    @Select("select * from lucky where qq = #{qq} order by `date` desc")
    List<Lucky> findByQQ(@Param("qq") String qq);
}
