package io.koschicken.database.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("lottery")
public class Lottery implements Serializable {

    private static final long serialVersionUID = -6392522183869205075L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String result;

    private Date date;
}
