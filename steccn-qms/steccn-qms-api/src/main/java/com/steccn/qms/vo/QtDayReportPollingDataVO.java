package com.steccn.qms.vo;

import com.steccn.pms.vo.FurtherValueStrVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;

/**
 * @author mengle
 * @desc 质量日报表-巡检-缺陷项
 */
@Data
@ApiModel("质量日报表-巡检-缺陷项")
public class QtDayReportPollingDataVO implements Serializable {

    @ApiModelProperty("类型  0正常元素  1合计 2总计")
    private Integer type;

    @ApiModelProperty("产线名称")
    private String productLineName;

    @ApiModelProperty("班次id 0早班 1夜班")
    private Integer classId;

    @ApiModelProperty("班次名称")
    private String className;

    @ApiModelProperty("数据")
    private List<FurtherValueStrVO> module;

}
