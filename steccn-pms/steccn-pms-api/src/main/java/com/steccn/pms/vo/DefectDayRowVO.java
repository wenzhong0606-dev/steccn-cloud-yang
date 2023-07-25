package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author yequan
 * @desc 不良日报表行视图对象
 * @date 2023/7/21
 */
@Data
@ApiModel("不良日报表行视图对象")
public class DefectDayRowVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("类型 0-数据行 1-产线合计 2-总计")
    private Integer type;

    @ApiModelProperty("产线")
    private String productLineName;

    @ApiModelProperty("班次日期")
    private String classDateStr;

    @ApiModelProperty("班次名称")
    private String className;

    @ApiModelProperty("班组名称")
    private String classGroupName;

    @ApiModelProperty("生产订单号")
    private String produceOrderNo;

    @ApiModelProperty("客户名称")
    private String customerName;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("规格，使用量，入库数，不良品，漏检，原片不良，磨边不良，钻孔不良，丝印不良，镀膜不良，钢化不良")
    private List<FurtherValueStrVO> module;

    @ApiModelProperty("不良率")
    private BigDecimal defectRate;
}
