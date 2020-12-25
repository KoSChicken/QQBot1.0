package io.koschicken.database.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("lottery_bet")
public class LotteryBet {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String qq;

    @TableField("group_code")
    private String groupCode;

    private String lottery;

    private Date createTime;
}
