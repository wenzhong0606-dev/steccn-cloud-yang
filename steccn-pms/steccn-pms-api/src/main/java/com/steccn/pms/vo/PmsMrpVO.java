package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 物料需求计划VO
 *
 * @author Mongknife
 * @date 2022/11/4
 */
@ApiModel("物料需求计划")
@Data
public class PmsMrpVO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("总需求数量")
    private BigDecimal quantity;

    @ApiModelProperty("库存数量")
    private BigDecimal stockQuantity;

    @ApiModelProperty("状态 0：正常 1：关闭 2：删除")
    private String status;

    @ApiModelProperty("类型id")
    private Long typeId;

    @ApiModelProperty("类型名称")
    private String typeName;

    @ApiModelProperty("单位id")
    private Long unitId;

    @ApiModelProperty("单位名称")
    private String unitName;
}
