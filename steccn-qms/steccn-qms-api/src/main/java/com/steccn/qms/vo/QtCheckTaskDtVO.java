package com.steccn.qms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 数字孪生VO
 *
 * @author AndrewLuo
 * @date 2023/05/16
 */
@Data
public class QtCheckTaskDtVO {
    private String itemName;
    private BigDecimal value;
    private Integer itemId;
    private Integer productLineId;
    private LocalDateTime updateTime;
}
