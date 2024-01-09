package com.steccn.common.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 基片统计报表耗料
 *
 * @author AndrewLuo
 * @date 2024/01/02
 */
@Data
public class CommonReportDTO {
    /**
     * 名字
     */
    private String name;
    /**
     * 单位
     */
    private String unit;
    /**
     * 值
     */
    private Map<String, BigDecimal> valueMap;

}
