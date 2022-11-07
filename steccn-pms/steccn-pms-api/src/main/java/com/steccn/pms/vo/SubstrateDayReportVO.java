package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 */
@ApiModel("基片日报表返回vo")
@Data
public class SubstrateDayReportVO implements Serializable {

    @ApiModelProperty("数据集合")
    private List<SubstrateDayReportListVO> dataList;

    @ApiModelProperty("规格数")
    private Integer specsNum;

    @ApiModelProperty("白班夜班合计统计项数量")
    private Integer dataNum;

    @ApiModelProperty("表头")
    private SubstrateReportHeaderVO header;
}
