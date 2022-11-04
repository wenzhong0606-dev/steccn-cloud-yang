package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 */
@Data
@ApiModel("能耗分组集合数据")
public class EnergyGroupListVO implements Serializable {

    @ApiModelProperty("产线")
    private Integer productLineId;

    @ApiModelProperty("班组")
    private Long classGroupId;

    @ApiModelProperty("类型")
    private Integer type;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

}
