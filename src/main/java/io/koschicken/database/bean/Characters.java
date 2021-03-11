package io.koschicken.database.bean;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("characters")
public class Characters implements Serializable {

    private static final long serialVersionUID = 6503985789018451916L;

    private Integer code;

    private String name;

    private String profile;

    private Boolean duel;

    private String master;

    public Characters(Integer code, String name, String profile, Boolean duel, String master) {
        this.code = code;
        this.name = name;
        this.profile = profile;
        this.duel = duel;
        this.master = master;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public Boolean getDuel() {
        return duel;
    }

    public void setDuel(Boolean duel) {
        this.duel = duel;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }
}
