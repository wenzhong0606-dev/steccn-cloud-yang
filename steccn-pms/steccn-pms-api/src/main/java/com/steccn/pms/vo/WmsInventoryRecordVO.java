package com.steccn.pms.vo;

import com.steccn.platform.vo.EqpSpareVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/12/9
 */
@Data
public class WmsInventoryRecordVO {

    /**
     * 主键
     */
    @ApiModelProperty("主键")
    private Long id;

    /**
     * 类型id
     */
    @ApiModelProperty("类型id")
    private Long type;

    /**
     * 单据编码
     */
    @ApiModelProperty("编码")
    private String code;

    /**
     * 类型
     */
    @ApiModelProperty("类型名称")
    private String typeName;

    /**
     * 物料id
     */
    @ApiModelProperty("物料主键")
    private Long materialId;

    /**
     * 物料名称
     */
    @ApiModelProperty("物料名称")
    private String materialName;

    /**
     * 物料编码
     */
    @ApiModelProperty("物料编码")
    private String materialCode;

    /**
     * 物料规格
     */
    private String specification;

    /**
     * 仓库id
     */
    @ApiModelProperty("仓库id")
    private Long warehouseId;

    /**
     * 仓库名称
     */
    @ApiModelProperty("仓库名称")
    private String warehouseName;


    /**
     * 货架id
     */
    @ApiModelProperty("货架id")
    private Long shelfId;

    /**
     * 货架名称
     */
    @ApiModelProperty("货架名称")
    private String shelfName;

    /**
     * 变动数量
     */
    private BigDecimal variableQuantity;

    /**
     * 变动前总数量
     */
    private BigDecimal frontTotalQuantity;

    /**
     * 变动前可用数量
     */
    private BigDecimal frontAvailableQuantity;

    /**
     * 变动前锁定数量
     */
    private BigDecimal frontLockQuantity;

    /**
     * 变动前在途数量
     */
    private BigDecimal frontStockQuantity;

    /**
     * 变动后总数量
     */
    private BigDecimal backTotalQuantity;

    /**
     * 变动后可用数量
     */
    private BigDecimal backAvailableQuantity;

    /**
     * 变动后锁定数量
     */
    private BigDecimal backLockQuantity;

    /**
     * 变动后在途数量
     */
    private BigDecimal backStockQuantity;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人id
     */
    private Long updateBy;

    /**
     * 更新人名称
     */
    private String updateName;

    /**
     * 更新人时间
     */
    private Date updateTime;

}
