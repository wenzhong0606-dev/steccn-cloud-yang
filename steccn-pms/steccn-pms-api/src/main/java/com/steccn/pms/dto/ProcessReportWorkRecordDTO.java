package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author yequan
 * @desc 分页查询报工记录
 */
@Data
@ApiModel("分页查询报工记录")
public class ProcessReportWorkRecordDTO extends BasePageDTO {

    @ApiModelProperty("工位id")
    @NotNull(message = "工位不能为空")
    private Integer stationId;
}
