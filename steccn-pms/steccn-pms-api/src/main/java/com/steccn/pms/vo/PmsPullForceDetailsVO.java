package com.steccn.pms.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/7 10:53
 */
@Data
public class PmsPullForceDetailsVO {

    private Integer productLineId;

    private String productLineName;

    @ApiModelProperty("班次数量数量")
    List<PmsPullForceClassQuantity> pullForceClassQuantityList;

    @ApiModelProperty("合计")
    private BigDecimal total;

    @ApiModelProperty("单位名称")
    private String unitName;
}
