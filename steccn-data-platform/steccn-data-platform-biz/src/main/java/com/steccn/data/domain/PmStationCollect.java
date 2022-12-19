package com.steccn.data.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 生产管理-进出站计数器
 * @author yequan
 * @TableName pm_station_collect
 */
@TableName(value ="pm_station_collect")
@Data
public class PmStationCollect implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.ASSIGN_ID)
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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}