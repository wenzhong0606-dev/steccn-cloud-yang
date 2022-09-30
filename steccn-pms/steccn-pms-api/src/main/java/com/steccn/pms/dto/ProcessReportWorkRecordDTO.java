package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author yequan
 * @desc 分页查询报工记录
 */
@Data
@ApiModel("分页查询报工记录")
public class ProcessReportWorkRecordDTO extends BasePageDTO {

    @ApiModelProperty("工位id")
    private Long stationId;
}
