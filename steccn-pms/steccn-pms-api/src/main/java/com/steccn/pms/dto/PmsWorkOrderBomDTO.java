package com.steccn.pms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 生产订单-物料清单
 *
 * @author Mongknife
 * @date 2022/10/8
 */
@Data
@ApiModel("生产订单-物料清单")
public class PmsWorkOrderBomDTO {

    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("物料id")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "物料id不能为空")
    private Long moduleId;

    @ApiModelProperty("数量")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "数量不能为空")
    @DecimalMin(groups = {UpdateGroup.class, InsertGroup.class}, value = "0", message = "数量应大于0")
    @DecimalMax(groups = {UpdateGroup.class, InsertGroup.class}, value = "99999999.999", message = "数量大小应小于99999999.999")
    @Digits(groups = {UpdateGroup.class, InsertGroup.class}, integer = 8, fraction = 3, message = "整数位上限为8位，小数位上限位3位")
    private BigDecimal quantity;

    @ApiModelProperty("单位Id")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "单位不能为空")
    private Long unitId;

    @ApiModelProperty("工序Id")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "工序不能为空")
    private Long processId;

    @ApiModelProperty("固定数量")
    @DecimalMin(groups = {UpdateGroup.class, InsertGroup.class}, value = "0", message = "固定数量应大于0")
    @DecimalMax(groups = {UpdateGroup.class, InsertGroup.class}, value = "99999999.999", message = "固定数量大小应小于99999999.999")
    @Digits(groups = {UpdateGroup.class, InsertGroup.class}, integer = 8, fraction = 3, message = "固定数量整数位上限为8位，小数位上限位3位")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "固定数量不能为空")
    private Integer fixedQuantityFlag;

    @ApiModelProperty("副产品")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "副产品不能为空")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer coproductFlag;

    @ApiModelProperty("联产品")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "联产品不能为空")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer jointProductFlag;

    @ApiModelProperty("反冲")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "反冲不能为空")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer backCalculateFlag;

    @ApiModelProperty("工序报废百分比")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "工序报废百分比不能为空")
    @Max(value = 100, message = "百分比不能大于100")
    @Min(value = 0, message = "百分比不能小于0")
    private Float processScrapingPercent;

    @ApiModelProperty("组件报废百分比")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "组件报废百分比不能为空")
    @Max(value = 100, message = "百分比不能大于100")
    @Min(value = 0, message = "百分比不能小于0")
    private Float componentScrapingPercent;

    @ApiModelProperty("是否回收")
//    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "是否回收不能为空")
    private Integer recycleFlag;

    @ApiModelProperty("工艺路线组件id")
    private Long craftRouteModuleId;


}
