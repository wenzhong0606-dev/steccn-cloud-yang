package com.steccn.data.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc
 * @date 2023/5/15
 */
@Data
@ApiModel("仪表差值读数")
public class DwMeterReadDiffDTO implements Serializable {

    @ApiModelProperty("远传仪表编码")
    private String code;

    @ApiModelProperty("记录时间-开始")
    private LocalDateTime start;

    @ApiModelProperty("记录时间-结束")
    private LocalDateTime end;
}
