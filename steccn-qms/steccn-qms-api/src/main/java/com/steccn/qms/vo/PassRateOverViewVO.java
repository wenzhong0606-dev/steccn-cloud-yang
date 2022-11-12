package com.steccn.qms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/12 13:46
 */
@Data
public class PassRateOverViewVO {

    private String passRate;

    /**
     * 日环比%
     */
    private String dayValue;

    /**
     * 周同比%
     */
    private String weekValue;

    /**
     * 最近7天合格率
     */
    private List<BigDecimal> datePassList;
}
