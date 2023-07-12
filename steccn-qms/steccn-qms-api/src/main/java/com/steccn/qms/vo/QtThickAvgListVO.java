package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author yequan
 * @desc 平均厚度集合视图对象
 * @date 2023/7/12
 */
@Data
@ApiModel("平均厚度集合视图对象")
public class QtThickAvgListVO implements Serializable {

    @ApiModelProperty("产线名称")
    private String productLineName;

    @ApiModelProperty("班组名称")
    private String classGroupName;

    @ApiModelProperty("平均厚度")
    private List<List<BigDecimal>> valueList;

}
