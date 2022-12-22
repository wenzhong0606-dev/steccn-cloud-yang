package com.steccn.data.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 生产管理-进出站计数器
 */
@ApiModel("生产管理-进出站计数器")
@Data
public class DwStationCollectDTO implements Serializable {

    @ApiModelProperty("数据记录时间")
    private LocalDate date;

    @ApiModelProperty("编码")
    private List<String> codeList;

}
