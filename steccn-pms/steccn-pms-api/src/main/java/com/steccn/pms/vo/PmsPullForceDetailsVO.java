package com.steccn.pms.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenhuan
 * @date 2022/11/7 10:53
 */
@Data
public class PmsPullForceDetailsVO {

    private Long id;

    private Integer productLineId;

    private String productLineName;

    @ApiModelProperty("白班数量")
    private BigDecimal dayClassQuantity;

    @ApiModelProperty("夜班数量")
    private BigDecimal nightClassQuantity;

    @ApiModelProperty("合计")
    private BigDecimal total;
}
