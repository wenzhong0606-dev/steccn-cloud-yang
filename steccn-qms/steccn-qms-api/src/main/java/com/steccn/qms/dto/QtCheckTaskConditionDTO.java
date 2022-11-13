package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yequan
 */
@Data
@Builder
@ApiModel("检验任务查询条件dto")
public class QtCheckTaskConditionDTO implements Serializable {

    @ApiModelProperty("开始时间")
    private LocalDateTime startTime;

    @ApiModelProperty("结束时间")
    private LocalDateTime endTime;

    @ApiModelProperty("班次id 0为早班 1为晚班 3为全部")
    private Integer workOrderId;

    @ApiModelProperty("任务类型 来料检1/首检2/在线检3/巡检4/出货检5/化验抽检6/配料抽检7")
    private Integer taskType;

    @ApiModelProperty("车间id")
    private Integer productWorkShopId;

    @ApiModelProperty("车间")
    private String productWorkshopName;

    @ApiModelProperty("产线id")
    private Integer productLineId;

    @ApiModelProperty("类型:0柱状图,1饼状图")
    private Integer type;

    @ApiModelProperty("检验项id")
    private Integer taskId;


}
