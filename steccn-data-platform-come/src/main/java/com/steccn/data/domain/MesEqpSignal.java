package com.steccn.data.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 设备管理-设备启停信号
 * @author AndrewLuo
 * @TableName mes_eqp_signal
 */
@TableName(value ="mes_eqp_signal")
@Data
public class MesEqpSignal implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 设备编码
     */
    private String code;

    /**
     * 0/1 停止/运行
     */
    private Integer value;

    /**
     * 变化时间
     */
    private LocalDateTime timestamp;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}