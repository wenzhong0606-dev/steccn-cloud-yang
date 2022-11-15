package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 */
@ApiModel("深加工日报表-原料耗料")
@Data
public class FurtherIngredientReportVO implements Serializable {

    @ApiModelProperty("表头")
    private List<FurtherIngredientReportListVO> tableData;

    @ApiModelProperty("表头")
    private FurtherReportHeaderVO header;

    @ApiModelProperty("规格数")
    private Integer specsNum;
}
