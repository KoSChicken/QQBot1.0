package io.koschicken.database.bean;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("pic")
public class Pic implements Serializable {

    private static final long serialVersionUID = -5731692021951818137L;

    @TableId(value = "pid")
    private Integer pid;

    private Date lastSendTime;
}
