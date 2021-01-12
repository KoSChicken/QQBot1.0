package io.koschicken.database.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("characters")
public class Characters implements Serializable {

    private static final long serialVersionUID = 6503985789018451916L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer code;

    private String name;

    private String profile;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "Characters{" +
                "id=" + id +
                ", code=" + code +
                ", name='" + name + '\'' +
                ", profile='" + profile + '\'' +
                '}';
    }
}
