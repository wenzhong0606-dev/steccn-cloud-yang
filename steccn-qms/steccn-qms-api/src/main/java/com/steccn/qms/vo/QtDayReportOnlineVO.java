package com.steccn.qms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 质量日报表-在线检视图对象
 */
@Data
@ApiModel("质量日报表-在线检视图对象")
public class QtDayReportOnlineVO implements Serializable {

    @ApiModelProperty("表头")
    private QtDayReportTableHeadersVO headers;

    @ApiModelProperty("数据部分")
    private List<QtDayReportOnlineDataVO> tableData;

    @ApiModelProperty("落板时段")
    private List<QtCheckTaskDestoryOnlineVO> fallList;




}
