package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("分页查询报工记录")
public class ProcessReportWorkRecordDTO extends BasePageDTO {

    @ApiModelProperty("工位id")
    private Long stationId;
}
