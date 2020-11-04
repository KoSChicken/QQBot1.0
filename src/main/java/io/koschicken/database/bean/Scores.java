package io.koschicken.database.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

@TableName("scores")
public class Scores implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "qq")
    private Long qq;
    @TableField(value = "sign_flag")
    private Boolean signFlag;
    private Integer score;
    @TableField(value = "group_code")
    private String groupCode;

    public Long getQq() {
        return qq;
    }

    public void setQq(Long qq) {
        this.qq = qq;
    }

    public Boolean getSignFlag() {
        return signFlag;
    }

    public void setSignFlag(Boolean signFlag) {
        this.signFlag = signFlag;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    @Override
    public String toString() {
        return "Scores{" +
                "qq=" + qq +
                ", signFlag=" + signFlag +
                ", score=" + score +
                ", groupCode='" + groupCode + '\'' +
                '}';
    }
}
