package io.koschicken.database.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("scores")
public class Scores implements Serializable {

    private static final long serialVersionUID = 3424842641174723836L;

    @TableId(value = "qq")
    private String qq;

    private String nickname;

    @TableField(value = "sign_flag")
    private Boolean signFlag;

    private Long score;

    @TableField(value = "live_flag")
    private Boolean liveFlag;

    @TableField(value = "roll_count")
    private Integer rollCount;

    @TableField(value = "cygames_win")
    private Integer cygamesWin;

    @TableField(value = "nekogun")
    private Integer nekogun;
}
