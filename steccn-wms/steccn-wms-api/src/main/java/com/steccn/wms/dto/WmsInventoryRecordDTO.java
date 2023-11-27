package com.steccn.wms.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存记录DTO
 *
 * @author Mongknife
 * @date 2022/12/9
 */
@Data
@ApiModel("库存记录DTO")
public class WmsInventoryRecordDTO extends BasePageDTO {


    /**
     * 主键
     */
    private Long id;

    /**
     * 变动类型
     */
    private Long type;

    /**
     * 单据编号
     */
    private String code;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 仓库id
     */
    private Integer warehouseId;

    /**
     * 货架id
     */
    private Integer shelfId;

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
}
