package com.steccn.data.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author mengle
 * @desc 生产管理-进出站计数器
 */
@ApiModel("生产管理-进出站计数器")
@Data
public class DwStationInfoDTO implements Serializable {

    @ApiModelProperty("数据记录开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("数据记录结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("编码")
    private List<String> codeList;



}
