package com.steccn.ems.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenhuan
 * @date 2022/12/5 15:25
 */
@Data
public class ReadCorrectVO {
    private Long id;
    private Long meterId;
    private LocalDateTime readTime;
    private BigDecimal readValue;
    private Integer adminFlag;
}
