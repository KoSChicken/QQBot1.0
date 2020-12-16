package io.koschicken.database.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

@TableName("scores")
public class Scores implements Serializable {

    private static final long serialVersionUID = 3424842641174723836L;

    @TableId(value = "qq")
    private String qq;

    private String nickname;

    @TableField(value = "sign_flag")
    private Boolean signFlag;

    private Integer score;

    @TableField(value = "live_flag")
    private Boolean liveFlag;

    @TableField(value = "roll_count")
    private Integer rollCount;

    @TableField(value = "cygames_win")
    private Integer cygamesWin;

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
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

    public Boolean getLiveFlag() {
        return liveFlag;
    }

    public void setLiveFlag(Boolean liveFlag) {
        this.liveFlag = liveFlag;
    }

    public Integer getRollCount() {
        return rollCount;
    }

    public void setRollCount(Integer rollCount) {
        this.rollCount = rollCount;
    }

    public Integer getCygamesWin() {
        return cygamesWin;
    }

    public void setCygamesWin(Integer cygamesWin) {
        this.cygamesWin = cygamesWin;
    }

    @Override
    public String toString() {
        return "Scores{" +
                "qq='" + qq + '\'' +
                ", nickname='" + nickname + '\'' +
                ", signFlag=" + signFlag +
                ", score=" + score +
                ", liveFlag=" + liveFlag +
                ", rollCount=" + rollCount +
                ", cygamesWin=" + cygamesWin +
                '}';
    }
}
