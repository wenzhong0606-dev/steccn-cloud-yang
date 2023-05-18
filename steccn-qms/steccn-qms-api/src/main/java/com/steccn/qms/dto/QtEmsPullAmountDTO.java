package com.steccn.qms.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ems专用拉引量DTO
 *
 * @author AndrewLuo
 * @date 2023/05/18
 */
@Data
public class QtEmsPullAmountDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 班次日期
     */
    @NotNull(message = "班次日期不能为空")
    private LocalDate classDate;
    /**
     * 早班0 晚班1
     */
    @NotNull(message = "班次不能为空")
    private Integer classId;
    /**
     * 产线id
     */
    @NotNull(message = "产线id不能为空")
    private Integer productLineId;
    /**
     * 生产订单号
     */
    private String productOrder;
    /**
     * 班次生产开始时间
     */
    @NotNull(message = "班次生产开始时间不能为空")
    private LocalDateTime startTime;
    /**
     * 班次生产结束时间
     */
    @NotNull(message = "班次生产开始时间不能为空")
    private LocalDateTime endTime;
    /**
     * 拉引量
     */
    private BigDecimal pullAmount;
}
