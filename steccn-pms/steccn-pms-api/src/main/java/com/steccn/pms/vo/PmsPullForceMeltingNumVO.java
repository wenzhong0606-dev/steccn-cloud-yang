package com.steccn.pms.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 拉引量熔化率VO
 *
 * @author AndrewLuo
 * @date 2023/12/04
 */
@Data
public class PmsPullForceMeltingNumVO {
    private BigDecimal pullForce;
    private BigDecimal weightCount;
    private BigDecimal percent;
}
