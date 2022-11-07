package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 */
@Data
@ApiModel("深加工日报表vo")
public class FurtherReportHeaderVO implements Serializable {

    @ApiModelProperty("规格")
    private BaseReportHeaderVO specList;

    @ApiModelProperty("表头模块")
    private List<BaseReportHeaderVO> module;

}
