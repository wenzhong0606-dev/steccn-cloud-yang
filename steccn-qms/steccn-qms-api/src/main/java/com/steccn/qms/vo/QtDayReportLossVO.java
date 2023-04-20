package com.steccn.qms.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 损耗图表VO
 *
 * @author AndrewLuo
 * @date 2023/04/20
 */
@Data
public class QtDayReportLossVO implements Serializable {
    @ApiModelProperty("表头")
    private QtDayReportTableHeadersVO headers;

    @ApiModelProperty("数据部分")
    private List<QtDayReportLossDataVO> tableData;
}
