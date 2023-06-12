package com.steccn.ems.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 能耗日统计报表VO
 * @date 2023/6/1
 */
@Data
@ApiModel("能耗日统计报表VO")
public class EpDayReportCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("表头")
    private EpDayReportCountHeadVO head;

    @ApiModelProperty("数据汇总")
    private List<EpDayReportCountRowVO> dataList;

    @ApiModelProperty("提示产线集合")
    private List<String> hintLineList;
}
