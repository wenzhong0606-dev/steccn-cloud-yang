package com.steccn.data.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 能源绩效-远程仪表采样表
 * @author yequan
 * @TableName ep_meter_collect
 */
@Data
public class EpMeterCollectDTO implements Serializable {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 编码
     */
    private String code;

    /**
     * 数据记录时间
     */
    private LocalDateTime recordTime;

    /**
     * 抄表值
     */
    private BigDecimal value;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}