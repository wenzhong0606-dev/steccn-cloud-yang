package com.steccn.ems.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 能耗历史对比
 */
@ApiModel("能耗历史对比")
@Data
public class ConsumeHistoryContrastDetailVO extends EpDetailVO implements Serializable {

    @ApiModelProperty("能耗数据")
    private String consumeStr;
}

