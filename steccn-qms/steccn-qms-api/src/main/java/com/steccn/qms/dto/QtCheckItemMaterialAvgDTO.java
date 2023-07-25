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
@ApiModel("检验项物料厚度数据传输对象")
public class QtCheckItemMaterialAvgDTO implements Serializable {

    @ApiModelProperty("产线主键")
    private Integer productLineId;

    @ApiModelProperty("班次-月")
    private String classMonth;

    @ApiModelProperty("班次日期")
    private LocalDate classDate;

    @ApiModelProperty("班次日期")
    private Integer classId;

    @ApiModelProperty("物料主键")
    private Long materialId;

    @ApiModelProperty("班组主键")
    private Integer classGroupId;

    @ApiModelProperty("工位")
    private String station;

    @ApiModelProperty("值")
    private BigDecimal value;

    @ApiModelProperty("编码")
    private String code;

}
