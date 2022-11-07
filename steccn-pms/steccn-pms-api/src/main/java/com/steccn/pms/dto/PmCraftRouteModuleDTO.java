package com.steccn.pms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工艺路线物料组件DTO
 *
 * @author Mongknife
 * @date 2022/9/7
 */
@Data
@ApiModel("工艺路线物料组件DTO")
public class PmCraftRouteModuleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键id")
    @NotNull(groups = UpdateGroup.class, message = "编辑数据时主键不能为空")
    private Long id;


    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "组件物料id不能为空")
    @ApiModelProperty("物料组件id")
    private Long materialId;


    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "组件物料编码不能为空")
    @ApiModelProperty("物料编码")
    private String materialCode;


    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "组件物料名称不能为空")
    @ApiModelProperty("物料名称")
    private String materialName;


//    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "物料数量不能为空")
    @ApiModelProperty("物料数量")
    @DecimalMin(groups = {UpdateGroup.class, InsertGroup.class},value="0",message="组件物料数量数量应大于0")
    @DecimalMax(groups = {UpdateGroup.class, InsertGroup.class},value="99999999.999",message="组件物料数量大小应小于99999999.999")
    @Digits(groups = {UpdateGroup.class, InsertGroup.class},integer = 8,fraction = 3,message = "组件物料数量整数位上限为8位，小数位上限位3位")
    private Integer quantity;


//    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "单位不能为空")
    @ApiModelProperty("单位id")
    private Long unitId;


//    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "固定数量不能为空")
    @ApiModelProperty("是否是固定数量：1是0否")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer isFixedQuantity;

//    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "副产品不能为空")
    @ApiModelProperty("是否是副产品：1是0否")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer isCopruduct;

//    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "联产品不能为空")
    @ApiModelProperty("是否是联产品：1是0否")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer isJointProduct;

//    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "反冲不能为空")
    @ApiModelProperty("是否是反冲：1是0否")
    @Max(value = 1, message = "状态只能是 0否  1是")
    @Min(value = 0, message = "状态只能是 0否  1是")
    private Integer isBackCalculation;

    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "工序报废百分比不能为空")
    @ApiModelProperty("工序报废百分比")
    @Max(value = 100, message = "百分比不能大于100")
    @Min(value = 0, message = "百分比不能小于0")
    private Float processScrapingPercent;

    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "组件报废不能为空")
    @Max(value = 100, message = "百分比不能大于100")
    @Min(value = 0, message = "百分比不能小于0")
    @ApiModelProperty("组件报废百分比")
    private Float componentScrapingPercent;

    @ApiModelProperty("工序信息Id")
    private Long processId;

    @ApiModelProperty("工序名称")
    private String processName;

    @ApiModelProperty("工艺路线id")
    private Long craftRouteId;

}
