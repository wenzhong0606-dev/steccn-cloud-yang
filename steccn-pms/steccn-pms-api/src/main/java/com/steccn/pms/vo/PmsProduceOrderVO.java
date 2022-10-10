package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 生产订单vo
 *
 * @author Mongknife
 * @date 2022/10/8
 */
@Data
@ApiModel("生产订单VO")
public class PmsProduceOrderVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("类型id")
    private Long typeId;

    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    private String materialName;

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

    @ApiModelProperty("单位")
    private String unit;

    @ApiModelProperty("产线id")
    private Long productionLineId;

    @ApiModelProperty("产线名称")
    private String productionLineName;

    @ApiModelProperty("状态")
    private Integer state;

    @ApiModelProperty("工艺id")
    private Long craftId;

    @ApiModelProperty("工序信息")
    private List<PmsProduceOrderCraftVO> processList;

    @ApiModelProperty("物料信息")
    private List<PmsWorkOrderBomVO> materialList;

    @ApiModelProperty("默认货架id(完工信息)")
    private Long defaultShelfId;

    @ApiModelProperty("默认仓库id(完工信息)")
    private Long defaultWarehouseId;

}
