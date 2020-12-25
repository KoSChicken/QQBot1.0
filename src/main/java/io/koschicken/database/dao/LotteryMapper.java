package io.koschicken.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.koschicken.database.bean.Lottery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface LotteryMapper extends BaseMapper<Lottery> {

    @Select("select * from lottery where group_code = #{groupCode}")
    Lottery today(String groupCode);
}
