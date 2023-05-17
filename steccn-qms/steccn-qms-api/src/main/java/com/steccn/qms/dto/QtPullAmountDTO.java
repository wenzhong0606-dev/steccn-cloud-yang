package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 计算拉引量DTO
 *
 * @author AndrewLuo
 * @date 2023/05/17
 */
@Data
public class QtPullAmountDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigDecimal pullAmount;

    private Integer classId;

    private Integer productLineId;

    private String productLineName;

    private Integer productWorkShopId;

    private String productWorkShopName;

    private LocalDate classDate;
}
