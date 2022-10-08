package com.steccn.pms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
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

    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("物料id")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "物料id不能为空")
    private Long materialId;

    @ApiModelProperty("物料名称")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "物料名称不能为空")
    private String materialName;

    @ApiModelProperty("物料名称")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "物料名称不能为空")
    private String materialCode;


    @ApiModelProperty("计划开始时间")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "计划开始时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime planStartTime;

    @ApiModelProperty("计划结束时间")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "计划结束时间不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime planEndTime;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("单位id")
    private Long unitId;

    @ApiModelProperty("产线id")
    private Long productionLineId;

    @ApiModelProperty("状态")
    private Integer state;

    @ApiModelProperty("工艺id")
    private Long craftId;

    @ApiModelProperty("默认仓库id")
    private Long defaultWarehouseId;

    @ApiModelProperty("工序信息")
    private List<PmsProduceOrderCraftDTO> processList;

    @ApiModelProperty("物料信息")
    private List<PmsWorkOrderBomDTO> materialList;
}
