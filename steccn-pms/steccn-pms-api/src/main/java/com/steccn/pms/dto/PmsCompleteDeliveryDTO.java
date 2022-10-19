package com.steccn.pms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/10/10
 */
@Data
@ApiOperation("投料信息Dto")
public class PmsCompleteDeliveryDTO extends BasePageDTO {

    @ApiModelProperty("订单id")
    private Long produceOrderId;

    @ApiModelProperty("工序id")
    private Long processId;

    @ApiModelProperty("开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    @ApiModelProperty("物料")
    private String material;
}
