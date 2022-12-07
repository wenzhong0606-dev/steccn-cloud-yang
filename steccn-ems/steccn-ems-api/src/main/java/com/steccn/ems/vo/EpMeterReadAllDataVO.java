package com.steccn.ems.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 全量数据表
 */
@Data
@ApiModel("全量数据表")
public class EpMeterReadAllDataVO implements Serializable {

    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("仪表id")
    private Integer meterId;

    @ApiModelProperty("机构树id")
    private Long officeId;

    @ApiModelProperty("抄表日期")
    private LocalDate readDate;

    @ApiModelProperty("班次id 0早班 1晚班")
    private Integer classId;

    @ApiModelProperty("厚度")
    private BigDecimal thick;

    @ApiModelProperty("开始时间")
    private LocalDateTime start;

    @ApiModelProperty("结束时间")
    private LocalDateTime end;

    @ApiModelProperty("管理员操作的值")
    private BigDecimal consume;

    @ApiModelProperty("管理员操作时间")
    private LocalDateTime adminTime;

    @ApiModelProperty("管理员调整标识 0否 1是")
    private Integer adminFlag;

    private static final long serialVersionUID = 1L;

}
