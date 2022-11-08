package com.steccn.pms.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author chenhuan
 * @date 2022/11/7 10:39
 */
@Data
public class PmsPullForceVO {

    @ApiModelProperty("车间id")
    private Integer workshopId;

    @ApiModelProperty("白班总量")
    private BigDecimal dayClassTotal;

    @ApiModelProperty("夜班总量")
    private BigDecimal nightClassTotal;

    @ApiModelProperty("总量")
    private BigDecimal total;

    @ApiModelProperty("日期")
    private LocalDate pullForceDate;

    @ApiModelProperty("单位名称")
    private String unitName;
}
