package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author yequan
 * @desc 厚度数据传输对象
 * @date 2023/7/12
 */
@Data
@ApiModel("厚度数据传输对象")
public class QtThickAvgDTO implements Serializable {

    @ApiModelProperty("产线主键")
    private Integer productLineId;

    @ApiModelProperty("班次日期")
    private LocalDate classDate;

    @ApiModelProperty("物料主键")
    private Long materialId;

    @ApiModelProperty("班组主键")
    private Integer classGroupId;

    @ApiModelProperty("值")
    private BigDecimal value;

}
