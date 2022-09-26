package com.steccn.pms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;


/**
 * 物料清单-物料组件DTO
 *
 * @author Mongknife
 * @date 2022/9/6
 */
@Data
@ApiModel("物料清单-物料组件DTO")
public class PmMaterialDTO {

    private static final long serialVersionUID = 1L;

    /**
     * 表ID
     */
    @NotNull(groups = UpdateGroup.class, message = "编辑数据时主键不能为空")
    @ApiModelProperty("主键id")
    private Long id;

    /**
     * 物料清单ID
     */
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "物料清单id不能为空")
    @ApiModelProperty("物料清单ID")
    private Long bomId;

    @ApiModelProperty("物料id")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "物料id不能为空")
    private Long materialId;

    /**
     * 物料编码
     */
    @ApiModelProperty("物料编码")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "物料编码不能为空")
    private String code;

    /**
     * 物料名称
     */
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "物料名称不能为空")
    @ApiModelProperty("物料名称")
    private String name;

    /**
     * 物料数量
     */
    @ApiModelProperty("物料数量")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "物料数量不能为空")
    private Integer quantity;

    /**
     * 单位ID
     */
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "单位不能为空")
    @ApiModelProperty("单位id")
    private Long unitId;

    /**
     * 是否是固定数量：1是0否
     */
    @Max(value = 1, message = "状态只能是 1是  0否")
    @Min(value = 0, message = "状态只能是 1是  0否")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "是否是固定数量不能为空")
    @ApiModelProperty("是否是固定数量：1是0否")
    private Integer isFixedQuantity;

    /**
     * 是否是副产品：1是0否
     */
    @Max(value = 1, message = "状态只能是 1是  0否")
    @Min(value = 0, message = "状态只能是 1是  0否")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "是否是副产品不能为空")
    @ApiModelProperty("是否是副产品：1是0否")
    private Integer isCopruduct;

    /**
     * 是否是联产品：1是0否
     */
    @Max(value = 1, message = "状态只能是 1是  0否")
    @Min(value = 0, message = "状态只能是 1是  0否")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "是否是联产品不能为空")
    @ApiModelProperty("是否是联产品：1是0否")
    private Integer isJointProduct;

    /**
     * 是否是反冲：1是0否
     */
    @Max(value = 1, message = "状态只能是 1是  0否")
    @Min(value = 0, message = "状态只能是 1是  0否")
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "是否是反冲不能为空")
    @ApiModelProperty("是否是反冲：1是0否")
    private Integer isBackCalculation;

    /**
     * 工序报废百分比
     */
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "工序报废百分比不能为空")
    @ApiModelProperty("工序报废百分比")
    private Float processScrapingPercent;

    /**
     * 组件报废百分比
     */
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "组件报废百分比不能为空")
    @ApiModelProperty("组件报废百分比")
    private Float componentScrapingPercent;

    /**
     * 用于区分导入时Bom
     */
    @ApiModelProperty("物料清单编码")
    private String bomCode;

    @ApiModelProperty("单位信息")
    private String unit;

}
