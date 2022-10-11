package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/10/10
 */
@Data
@ApiModel("生产订单 -进站信息")
public class PmsProcessInStationDTO extends BasePageDTO {

    @ApiModelProperty("工序id")
    private Long processId;

    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("工位id")
    private Integer stationId;
}
