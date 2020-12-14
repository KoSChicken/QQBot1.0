package io.koschicken.database.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

@TableName("qq_group")
public class QQGroup implements Serializable {

    private static final long serialVersionUID = -7334313875124373777L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String qq;

    @TableField(value = "group")
    private String groupCode;

    public QQGroup(String qq, String group) {
        this.qq = qq;
        groupCode = group;
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

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    @Override
    public String toString() {
        return "QQGroup{" +
                "id=" + id +
                ", qq='" + qq + '\'' +
                ", group='" + groupCode + '\'' +
                '}';
    }
}
