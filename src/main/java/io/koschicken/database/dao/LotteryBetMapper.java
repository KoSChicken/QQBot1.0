package io.koschicken.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.koschicken.database.bean.LotteryBet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface LotteryBetMapper extends BaseMapper<LotteryBet> {

    @Select("select * from lottery_bet " +
            "where date((create_time / 1000), 'unixepoch', 'localtime') = date(CURRENT_TIMESTAMP,'localtime') " +
            "and group_code = #{groupCode}")
    List<LotteryBet> listToday(String groupCode);

    @Select("select * from lottery_bet where lottery = #{lottery} and group_code = #{groupCode}")
    List<LotteryBet> lottery(String lottery, String groupCode);

    @Select("select * from lottery_bet where qq = #{qq} " +
            "and group_code = #{groupCode} " +
            "and date((create_time / 1000), 'unixepoch', 'localtime') = date(CURRENT_TIMESTAMP,'localtime') " +
            "order by id desc limit 1")
    LotteryBet findByQQAndGroupCode(String qq, String groupCode);
}
