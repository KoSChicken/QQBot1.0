package io.koschicken.database.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
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
}
