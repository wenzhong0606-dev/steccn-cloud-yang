package com.steccn.pms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author yequan
 */
@Data
public class WorkOrderBatchVO implements Serializable {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 批次号
     */
    private String code;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单位id
     */
    private Long unitId;

    /**
     * 是否当前批次（0：否 1：是）
     */
    private Integer currentBatchFlag;

    /**
     * 最小包装数量
     */
    private Integer minPackQuantity;

    /**
     * 生产订单id
     */
    private Long produceOrderId;

    private static final long serialVersionUID = 1L;
}
