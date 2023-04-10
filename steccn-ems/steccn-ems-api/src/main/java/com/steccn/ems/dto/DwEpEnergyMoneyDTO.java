package com.steccn.ems.dto;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 数仓-算钱
 * @date 2023/3/20
 */
@Data
@ApiModel("数仓-算钱")
public class DwEpEnergyMoneyDTO implements Serializable {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("班次日期")
    private LocalDate classDate;

    @ApiModelProperty("远传仪表编码")
    private String code;

    @ApiModelProperty("分区节点id")
    private Long partitionId;

    @ApiModelProperty("开始时间")
    private LocalDateTime start;

    @ApiModelProperty("结束时间")
    private LocalDateTime end;

    @ApiModelProperty("峰1 平2 谷3")
    private Integer type;

    @ApiModelProperty("开始读数")
    private BigDecimal startValue;

    @ApiModelProperty("结束读数")
    private BigDecimal endValue;

    @ApiModelProperty("单价")
    private BigDecimal price;

    private static final long serialVersionUID = 1L;

}
