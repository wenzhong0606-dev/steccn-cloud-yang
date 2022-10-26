package com.steccn.pms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报工信息分页DTO
 *
 * @author Mongknife
 * @date 2022/10/11
 */
@Data
@ApiModel("生产订单工艺-报工信息分页DTO")
public class PmsReportWorkPageDTO extends BasePageDTO {

    @ApiModelProperty("工序id")
    private Long processId;

    @ApiModelProperty("开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("工位id")
    private Integer stationId;

    @ApiModelProperty("订单id")
    private Long produceId;

}
