package com.steccn.wms.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import javax.naming.ldap.PagedResultsControl;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/12/20
 */
@Data
@ApiModel("获取物料的上架策略的仓库货架")
public class WmsMaterialShelfUpVO {


    /**
     * 物料id
     */
    @ApiModelProperty("物料id")
    private Long materialId;

    /**
     * 仓库id
     */
    @ApiModelProperty("仓库id")
    private Integer warehouseId;

    /**
     * 仓库名称
     */
    @ApiModelProperty("仓库名称")
    private String warehouseName;

    /**
     * 仓库编码
     */
    @ApiModelProperty("仓库编码")
    private String warehouseCode;

    /**
     * 货架id
     */
    @ApiModelProperty("货架id")
    private Integer shelfId;

    /**
     * 货架名称
     */
    @ApiModelProperty("货架名称")
    private String shelfName;

    /**
     * 货架编码
     */
    @ApiModelProperty("货架编码")
    private String shelfCode;
}
