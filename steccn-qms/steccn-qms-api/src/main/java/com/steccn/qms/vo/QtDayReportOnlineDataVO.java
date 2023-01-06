package com.steccn.qms.vo;

import com.steccn.pms.vo.FurtherValueStrVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * @author yequan
 * @desc 质量日报表-在线检数据视图对象
 */
@Data
@ApiModel("质量日报表-在线检数据视图对象")
public class QtDayReportOnlineDataVO implements Serializable {

    @ApiModelProperty("类型  0正常元素  1合计 2总计")
    private Integer type;

    @ApiModelProperty("数据来源")
    private Integer dataSource;

    @ApiModelProperty("数据来源 0人工 1机器采集")
    private String dataSourceStr;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("产线名称")
    private String productLineName;

    @ApiModelProperty("班次id 0早班 1夜班")
    private Integer classId;

    @ApiModelProperty("班次名称")
    private String className;

    @ApiModelProperty("缺陷记录时段-开始")
    private LocalTime defectStartTime;

    @ApiModelProperty("缺陷记录时段-结束")
    private LocalTime defectEndTime;

    @ApiModelProperty("缺陷记录时段")
    private String defectTimeStr;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("数据")
    private List<FurtherValueStrVO> module;

}
