package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 生产订单DTO
 *
 * @author Mongknife
 * @date 2022/10/8
 */
@Data
@ApiModel("生产订单")
public class PmsProduceOrderDTO {

    @ApiModelProperty("类型id")
    private Long typeId;

    @ApiModelProperty("物料id")
    private Integer materialId;

    @ApiModelProperty("计划开始时间")
    private LocalDateTime planStartTime;

    @ApiModelProperty("计划结束时间")
    private LocalDateTime planEndTime;

    @ApiModelProperty("实际开始时间")
    private LocalDateTime actualStartTime;

    @ApiModelProperty("实际结束时间")
    private LocalDateTime actualEndTime;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("单位id")
    private Long unitId;

    @ApiModelProperty("产线id")
    private Integer productionLineId;

    @ApiModelProperty("状态")
    private Integer state;

    @ApiModelProperty("工艺id")
    private Long craftId;

    @ApiModelProperty("默认仓库id")
    private Integer defaultWarehouseId;

    @ApiModelProperty("工序信息")
    private List<PmsProduceOrderCraftDTO> processList;

    @ApiModelProperty("物料信息")
    private List<PmsWorkOrderBomDTO> materialList;
}
