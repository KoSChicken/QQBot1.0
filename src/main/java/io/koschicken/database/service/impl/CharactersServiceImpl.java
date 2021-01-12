package io.koschicken.database.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.koschicken.database.bean.Characters;
import io.koschicken.database.dao.CharactersMapper;
import io.koschicken.database.service.CharactersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CharactersServiceImpl extends ServiceImpl<CharactersMapper, Characters> implements CharactersService {

    @Autowired
    CharactersMapper charactersMapper;

    @Override
    public Characters findByCode(Integer code) {
        return charactersMapper.findByCode(code);
    }

    @Override
    public List<Characters> list(Integer excludeCode, Integer limit) {
        return charactersMapper.list(excludeCode, limit);
    }
}
