package com.steccn.ems.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 生产看板能耗
 * @date 2023/8/18
 */
@Data
@ApiModel("生产看板能耗")
@AllArgsConstructor
@NoArgsConstructor
public class EpPmsConsumeVO implements Serializable {

    @ApiModelProperty("总数")
    private BigDecimal sum;

    @ApiModelProperty("单位")
    private String unitName;
}
