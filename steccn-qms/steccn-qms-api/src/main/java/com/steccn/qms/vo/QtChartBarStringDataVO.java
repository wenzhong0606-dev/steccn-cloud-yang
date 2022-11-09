package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 字符串类dataVO
 *
 * @author AndrewLuo
 * @date 2022/11/09
 */
@Data
public class QtChartBarStringDataVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * String类型
     */
    private List<String> data;
}
