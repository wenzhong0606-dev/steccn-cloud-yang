package com.steccn.dp.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 天然气流量Data
 *
 * @author AndrewLuo
 * @date 2024/05/27
 */
@Data
public class DpNatureGasVO {
    /**
     * 时间
     */
    private LocalDateTime time;
    /**
     * 测点id
     */
    private BigDecimal value;
    /**
     *
     */
    private String pointId;
}
