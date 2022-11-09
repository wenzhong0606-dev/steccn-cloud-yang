package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 */
@ApiModel("深加工日报表-产量-行数据")
@Data
public class FurtherProduceReportListVO implements Serializable {

    @ApiModelProperty("类型 0-数据 1-产线合计 2-车间合计 3-总合计")
    private Integer type;

    @ApiModelProperty("车间名称")
    private String workshopName;

    @ApiModelProperty("产线名称")
    private String productLineName;

    @ApiModelProperty("班次名称")
    private String className;

    @ApiModelProperty("规格")
    private List<String> specList;

    @ApiModelProperty("工序信息")
    private List<FurtherValueStrVO> module;

}
