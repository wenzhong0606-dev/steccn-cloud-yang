package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author yequan
 * @desc 能源全量数据表行数据
 * @date 2023/5/18
 */
@Data
@ApiModel("能源全量数据表行数据")
public class EpMeterReadRowDTO implements Serializable {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("仪表主键")
    private Integer meterId;

    @ApiModelProperty("远传表映射编码")
    private String distantMapCode;

    @ApiModelProperty("抄表日期")
    private LocalDate readDate;

    @ApiModelProperty("班次id 0早班 1晚班")
    private Integer classId;

    @ApiModelProperty("厚度")
    private BigDecimal thick;

    @ApiModelProperty("生产订单主键")
    private Long produceOrderId;

    @ApiModelProperty("开始时间")
    private LocalDateTime start;

    @ApiModelProperty("结束时间")
    private LocalDateTime end;

    @ApiModelProperty("抄表值")
    private BigDecimal readValue;

    @ApiModelProperty("报工集合")
    private Set<Long> reportWorkIdSet;

    @ApiModelProperty("产量")
    private BigDecimal yield;


    private static final long serialVersionUID = 1L;
}