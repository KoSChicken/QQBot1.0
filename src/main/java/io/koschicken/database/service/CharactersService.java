package io.koschicken.database.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.koschicken.database.bean.Characters;

import java.util.List;

public interface CharactersService extends IService<Characters> {
    Characters findByCode(Integer code);

    List<Characters> listAll();

    List<Characters> list(Integer excludeCode, Integer limit);

    void update(Integer code, String master);

    List<Characters> listByMaster(String master);

    String findMasterByName(String name);
}
