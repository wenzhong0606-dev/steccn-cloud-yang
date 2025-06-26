package com.steccn.wms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/12/9
 */
@Data
@ApiModel("仓储库存DTO")
public class WmsInventoryDTO {

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("原始数量")
    private BigDecimal originalQuantity;

    @ApiModelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("原始箱号")
    private String originalCaseNo;

    @ApiModelProperty("批次")
    private String batch;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 仓库id
     */
    private Integer warehouseId;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 货架id
     */
    private Integer shelfId;

    /**
     * 货架编码
     */
    private String shelfCode;

    /**
     * 货架名称
     */
    private String shelfName;

    /**
     * 总数
     */
    private BigDecimal totalQuantity;

    /**
     * 可用数量
     */
    private BigDecimal availableQuantity;

    /**
     * 在途库存
     */
    private BigDecimal stockQuantity;

    /**
     * 冻结库存
     */
    private BigDecimal freezeQuantity;

    /**
     * 锁定库存
     */
    private BigDecimal lockQuantity;

    /**
     * 质检库存
     */
    private BigDecimal detectionQuantity;

    /**
     * 单位id
     */
    private Long unit;


    /**
     * 打包时间
     */
    private LocalDateTime packTime;

    private Long updateBy;

    private String updateName;

    private Long createBy;

    private String createName;

}
