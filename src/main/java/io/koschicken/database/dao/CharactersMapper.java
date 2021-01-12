package io.koschicken.database.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.koschicken.database.bean.Characters;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface CharactersMapper extends BaseMapper<Characters> {
    @Select("select * from characters where code = #{code} and code <> 1000")
    Characters findByCode(Integer code);

    @Select("select * from characters where code <> #{excludeCode} and code <> 1000 limit #{limit}")
    List<Characters> list(Integer excludeCode, Integer limit);
}
