package io.koschicken.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.koschicken.database.bean.Characters;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface CharactersMapper extends BaseMapper<Characters> {
    @Select("select * from characters where code = #{code} and code <> 1000")
    Characters findByCode(Integer code);

    @Select("select * from characters where duel = 1 and master is null")
    List<Characters> listAll();

    @Select("select * from characters where code <> #{excludeCode} and code <> 1000 limit #{limit}")
    List<Characters> list(Integer excludeCode, Integer limit);

    @Update("update characters set master = #{master} where code = #{code}")
    void update(Integer code, String master);

    @Select("select * from characters where master = #{master}")
    List<Characters> listByMaster(String master);

    @Select("select master from characters where name like #{name}")
    String findMasterByName(String name);
}
