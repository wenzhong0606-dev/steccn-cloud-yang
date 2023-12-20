package com.steccn.platform.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author yequan
 * @desc 标准产能的视图对象
 * @date 2023/12/20
 */
@Data
public class SysProductionCapacityVO implements Serializable {

    /**
     * 主键
     */
    private Integer id;

    /**
     * 日期
     */
    private LocalDate classDate;

    /**
     * 标准产能
     */
    private BigDecimal standardCapacity;

    /**
     * 产线基数
     */
    private Integer lineNum;

    private static final long serialVersionUID = 1L;
}
