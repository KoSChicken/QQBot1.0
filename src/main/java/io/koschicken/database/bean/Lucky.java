package io.koschicken.database.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("lucky")
public class Lucky implements Serializable {

    private static final long serialVersionUID = 3146650876135600178L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Long qq;

    private Date date;

    private Integer coin;

    private Integer count;

    public Lucky(Long qq, Date date, Integer coin) {
        this.qq = qq;
        this.date = date;
        this.coin = coin;
    }
}
