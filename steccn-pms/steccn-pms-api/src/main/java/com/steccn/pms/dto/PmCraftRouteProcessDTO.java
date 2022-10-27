package com.steccn.pms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 工艺路线工序DTO
 *
 * @author Mongknife
 * @date 2022/9/7
 */
@Data
@ApiModel("工艺路线工序DTO")
public class PmCraftRouteProcessDTO implements Serializable {


    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("工序id")
    private Long processId;

    @ApiModelProperty("工序名称")
    private String processName;

    @ApiModelProperty("工艺路线id")
    private Long routeId;

    @ApiModelProperty("数量")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "工序数量不能为空")
    @DecimalMin(groups = {UpdateGroup.class, InsertGroup.class},value="0",message="工序数量数量应大于0")
    @DecimalMax(groups = {UpdateGroup.class, InsertGroup.class},value="99999999.999",message="工序数量大小应小于99999999.999")
    @Digits(groups = {UpdateGroup.class, InsertGroup.class},integer = 8,fraction = 3,message = "工序数量整数位上限为8位，小数位上限位3位")
    private BigDecimal quantity;

    @ApiModelProperty("数量单位")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "工序单位不能为空")
    private Long unitId;

    @ApiModelProperty("标准数量")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "工序标准不能为空")

    private BigDecimal standardQuantity;

    @ApiModelProperty("标准数量单位")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "工序标准单位不能为空")
    private Long standardUnitId;

    @ApiModelProperty("是否报工")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "工序是否报工不能为空")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer reportWorkFlag;

    @ApiModelProperty("是否报检")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "工序是否报检不能为空")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer reportCheckFlag;


    @ApiModelProperty("是否进站")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "工序是否进站不能为空")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer inStationFlag;


    @ApiModelProperty("是否出站")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "工序是否出站不能为空")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer outStationFlag;


    @ApiModelProperty("工序报废百分比")
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "工序报废百分比不能为空")
    @Max(value = 100, message = "百分比不能大于100")
    @Min(value = 0, message = "百分比不能小于0")
    private BigDecimal processScrap;

}
