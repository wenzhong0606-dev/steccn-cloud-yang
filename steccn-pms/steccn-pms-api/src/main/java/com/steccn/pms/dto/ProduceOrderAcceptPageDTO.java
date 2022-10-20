package com.steccn.pms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/10/13
 */
@ApiModel("订单接收")
@Data
public class ProduceOrderAcceptPageDTO extends BasePageDTO {

    @ApiModelProperty("开始时间")
    private LocalDate startTime;

    @ApiModelProperty("结束时间")
    private LocalDate endTime;

    @ApiModelProperty("实际开始时间")
    private LocalDateTime actualStartTime;

    @ApiModelProperty("实际结束时间")
    private LocalDateTime actualEndTime;

    @ApiModelProperty("产线id")
    private Integer productionLineId;

    @ApiModelProperty("状态")
    private Integer state;
}
