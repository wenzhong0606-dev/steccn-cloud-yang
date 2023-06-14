package com.steccn.pms.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class PmsCompleteDeliveryInfoDTO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 物料编码
     */
    private String materialCode;


    /**
     * 箱号
     */
    private String caseNo;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 装箱时间
     */
    private LocalDateTime packTime;

    /**
     * 装箱数量
     */
    private BigDecimal quantity;

    /**
     * 可用数量
     */
    private BigDecimal avaQuantity;

    /**
     * 单位id
     */
    private Long unitId;

    /**
     * 出库单id
     */
    private Long completeDeliveryId;

    /**
     * 工序id
     */
    private Long processId;

    /**
     * 仓库id
     */
    private Integer warehouseId;

    /**
     * 货架id
     */
    private Integer shelfId;

}
