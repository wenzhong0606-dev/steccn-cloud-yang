package com.steccn.qms.dto;

import com.steccn.qms.constant.QtManagerConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 不良报表请求对象
 * @date 2023/7/11
 */

@Data
@ApiModel("不良报表请求对象")
public class QtDefectReportDTO implements Serializable {

    @ApiModelProperty("产线集合")
    private List<Integer> productLineList;

    /**
     * @see QtManagerConstant.LossReportType
     */
    @ApiModelProperty("粒度")
    private Integer granularity;

    @ApiModelProperty("开始日期")
    private LocalDate startDate;

    @ApiModelProperty("结束日期")
    private LocalDate endDate;

}
