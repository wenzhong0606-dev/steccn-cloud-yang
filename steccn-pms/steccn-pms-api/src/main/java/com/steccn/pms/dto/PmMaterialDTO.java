package com.steccn.pms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


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
    @ApiModelProperty("主键id")
    private Long id;

    /**
     * 物料清单ID
     */
    @ApiModelProperty("物料清单ID")
    private Long bomId;

    @ApiModelProperty("物料id")
    private String materialId;

    /**
     * 物料编码
     */
    @ApiModelProperty("物料编码")
    private String code;

    /**
     * 物料名称
     */
    @ApiModelProperty("物料名称")
    private String name;

    /**
     * 物料数量
     */
    @ApiModelProperty("物料数量")
    private Integer quantity;

    /**
     * 单位ID
     */
    @ApiModelProperty("单位id")
    private Long unitId;

    /**
     * 是否是固定数量：1是0否
     */
    @ApiModelProperty("是否是固定数量：1是0否")
    private Integer isFixedQuantity;

    /**
     * 是否是副产品：1是0否
     */
    @ApiModelProperty("是否是副产品：1是0否")
    private Integer isCopruduct;

    /**
     * 是否是联产品：1是0否
     */
    @ApiModelProperty("是否是联产品：1是0否")
    private Integer isJointProduct;

    /**
     * 是否是反冲：1是0否
     */
    @ApiModelProperty("是否是反冲：1是0否")
    private Integer isBackCalculation;

    /**
     * 工序报废百分比
     */
    @ApiModelProperty("工序报废百分比")
    private Integer processScrapingPercent;

    /**
     * 组件报废百分比
     */
    @ApiModelProperty("组件报废百分比")
    private Integer componentScrapingPercent;

}
