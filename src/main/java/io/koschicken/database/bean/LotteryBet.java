package io.koschicken.database.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("lottery_bet")
public class LotteryBet {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String qq;

    private String groupCode;

    private String lottery;
}
