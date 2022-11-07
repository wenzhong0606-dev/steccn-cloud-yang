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
@ApiModel("深加工日报表-产量")
public class FurtherProduceReportVO implements Serializable {

    @ApiModelProperty("数据集合")
    private List<FurtherProduceReportListVO> tableData;

    @ApiModelProperty("表头")
    private FurtherReportHeaderVO header;

}
