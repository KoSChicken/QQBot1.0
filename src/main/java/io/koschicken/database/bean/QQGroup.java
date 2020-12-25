package io.koschicken.database.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("qq_group")
public class QQGroup implements Serializable {

    private static final long serialVersionUID = -7334313875124373777L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String qq;

    @TableField(value = "group_code")
    private String groupCode;

    public QQGroup(String qq, String group) {
        this.qq = qq;
        groupCode = group;
    }
}
