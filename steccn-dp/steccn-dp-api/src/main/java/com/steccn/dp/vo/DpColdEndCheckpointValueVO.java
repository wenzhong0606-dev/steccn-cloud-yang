package com.steccn.dp.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 测点数据code
 *
 * @author AndrewLuo
 * @date 2024/03/01
 */
@Data
public class DpColdEndCheckpointValueVO {
    /**
     * 测点code
     */
    private String checkPointCode;
    /**
     * 测点名称
     */
    private String checkPointName;
    /**
     * 时间戳
     */
    private LocalDateTime timeStamp;
    /**
     * 值
     */
    private BigDecimal value;
}
