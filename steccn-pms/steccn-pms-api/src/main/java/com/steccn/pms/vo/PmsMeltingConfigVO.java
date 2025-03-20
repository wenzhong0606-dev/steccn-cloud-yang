package com.steccn.pms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 熔化率VO
 *
 * @author AndrewLuo
 * @date 2025/03/20
 */
@Data
public class PmsMeltingConfigVO {
    /**
     *
     */
    private BigDecimal meltingNum;

    /**
     *
     */
    private LocalDate meltingDate;
}
