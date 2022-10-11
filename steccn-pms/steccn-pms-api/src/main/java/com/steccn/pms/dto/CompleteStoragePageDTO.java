package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDate;

/**
 * @author yequan
 * @desc 入库记录分页vo
 */
@Data
@ApiModel("入库记录分页vo")
public class CompleteStoragePageDTO extends BasePageDTO {

    @ApiModelProperty("产线id")
    private Integer productLineId;

    @ApiModelProperty("开始时间")
    private LocalDate start;

    @ApiModelProperty("结束时间")
    private LocalDate end;
}
