package io.koschicken.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.koschicken.database.bean.Lottery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface LotteryMapper extends BaseMapper<Lottery> {

    @Select("select * from lottery " +
            "where group_code = #{groupCode} " +
            "and date((create_time/1000), 'unixepoch', 'localtime') = date(CURRENT_TIMESTAMP, '-1 day', 'localtime') " +
            "order by id desc limit 1")
    Lottery today(String groupCode);
}
