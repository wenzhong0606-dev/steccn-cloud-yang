package com.steccn.ems.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 能源电耗
 * @date 2023/8/25
 */
@Data
@ApiModel("能源电耗")
public class PmElectricConsumeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("x轴名称")
    private String xsName;

    @ApiModelProperty("值")
    private BigDecimal value;
}
