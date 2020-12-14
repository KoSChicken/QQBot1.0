package io.koschicken.database.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

@TableName("live")
public class Live implements Serializable {

    private static final long serialVersionUID = 3402006879767506885L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String qq;

    @TableField(value = "bili_uid")
    private String biliUid;

    public Live(String qq, String biliUid) {
        this.qq = qq;
        this.biliUid = biliUid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getBiliUid() {
        return biliUid;
    }

    public void setBiliUid(String biliUid) {
        this.biliUid = biliUid;
    }

    @Override
    public String toString() {
        return "Live{" +
                "id=" + id +
                ", qq='" + qq + '\'' +
                ", biliUid='" + biliUid + '\'' +
                '}';
    }
}
