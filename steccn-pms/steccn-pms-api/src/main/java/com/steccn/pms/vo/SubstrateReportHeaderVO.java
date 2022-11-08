package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yequan
 */
@Data
@ApiModel("基片日报表表头vo")
public class SubstrateReportHeaderVO implements Serializable {

    @ApiModelProperty("规格")
    private BaseReportHeaderVO specList;

    @ApiModelProperty("白班")
    private BaseReportHeaderVO dayShift;

    @ApiModelProperty("夜班")
    private BaseReportHeaderVO nightShift;

    @ApiModelProperty("合计")
    private BaseReportHeaderVO total;
}
