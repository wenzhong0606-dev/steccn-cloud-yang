package com.steccn.dp.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.steccn.common.constant.ResultCode;
import com.steccn.dp.dto.DpWmsDataDTO;
import lombok.Data;

/**
 * 仓储期末库存统计
 * @TableName ads_wms_inventory
 */
@TableName(value ="ads_wms_inventory")
@Data
public class AdsWmsInventory implements Serializable {
    /**
     * 
     */
    @TableId
    private Long materialId;

    /**
     * 
     */
    private Integer warehouseId;

    /**
     * 
     */
    private Integer shelfId;

    /**
     * 
     */
    private Long unit1;

    /**
     * 
     */
    private Date dt;

    /**
     * 
     */
    private String materialCode;

    /**
     * 
     */
    private String materialName;

    /**
     * 
     */
    private String warehouseCode;

    /**
     * 
     */
    private String warehouseName;

    /**
     * 
     */
    private String shelfCode;

    /**
     * 
     */
    private String shelfName;

    /**
     * 
     */
    private BigDecimal totalQuantity1;

    /**
     * 
     */
    private BigDecimal totalQuantity2;

    /**
     * 
     */
    private Long unit2;

    /**
     * 
     */
    private BigDecimal totalQuantity3;

    /**
     * 
     */
    private Long unit3;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}