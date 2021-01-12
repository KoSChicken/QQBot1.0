package io.koschicken.database.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("lottery")
public class Lottery implements Serializable {

    private static final long serialVersionUID = 8104092891296268354L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String result;

    private Date createTime;

    @TableField(value = "group_code")
    private String groupCode;

    @TableField(value = "current_reward")
    private Long currentReward;
}
