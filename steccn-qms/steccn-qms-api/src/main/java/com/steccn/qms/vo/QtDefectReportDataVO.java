package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 不良报表视图对象
 * @date 2023/7/11
 */
@Data
@ApiModel("不良报表视图对象")
public class QtDefectReportDataVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("类型 0表示数据 1表示合计（单产线） 2表示总计")
    private Integer type;

    @ApiModelProperty("产线")
    private Integer productLine;

    @ApiModelProperty("产线名称")
    private String productLineName;

    @ApiModelProperty("班次日期")
    private String classDateStr;

    @ApiModelProperty("班次")
    private Integer classId;

    @ApiModelProperty("班次名称")
    private String className;

    @ApiModelProperty("班组")
    private Integer classGroupId;

    @ApiModelProperty("班组名称")
    private String classGroupName;

    @ApiModelProperty("缺陷数量集合")
    private List<String> valueList;
}
