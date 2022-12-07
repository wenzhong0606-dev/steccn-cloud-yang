package com.steccn.ems.dto;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @desc 全量数据表
 * @author yequan
 */
@Data
@ApiModel("全量数据表")
public class EpMeterReadAllDataVO implements Serializable {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 仪表id
     */
    private Integer meterId;

    /**
     * 抄表日期
     */
    private LocalDate readDate;

    /**
     * 班次id 0早班 1晚班
     */
    private Integer classId;

    /**
     * 厚度
     */
    private Long thick;

    /**
     * 管理员操作值
     */
    private BigDecimal adminValueStart;

    private BigDecimal adminValueEnd;

    /**
     * 管理员操作时间
     */
    private LocalDateTime adminTime;

    /**
     * 管理员调整标识 0否 1是
     */
    private Integer adminFlag;

    private static final long serialVersionUID = 1L;

}
