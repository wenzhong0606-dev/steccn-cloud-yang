package com.steccn.ems.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/12/16 17:12
 */
@Data
public class EpDeepConsumeContrastVO {
    private List<String> xAxis;
    private List<BigDecimal> yData;
    private BigDecimal standardValue;
}
