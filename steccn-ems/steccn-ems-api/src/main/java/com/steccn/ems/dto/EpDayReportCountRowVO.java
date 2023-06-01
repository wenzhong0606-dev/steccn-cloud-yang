package com.steccn.ems.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 能耗日统计报表-行数据
 * @date 2023/6/1
 */
@Data
@ApiModel("能耗日统计报表-行数据")
public class EpDayReportCountRowVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("能源类型")
    private String energyType;

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("部门")
    private String departmentName;

    @ApiModelProperty("能耗")
    private List<String> consumeList;

    @ApiModelProperty("总计")
    private String sumStr;

}
