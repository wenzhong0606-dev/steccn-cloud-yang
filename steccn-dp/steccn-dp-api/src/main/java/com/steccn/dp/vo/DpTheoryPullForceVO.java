package com.steccn.dp.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 理论拉引量
 *
 * @author AndrewLuo
 * @date 2024/06/26
 */
@Data
public class DpTheoryPullForceVO {
    /**
     * 玻璃密度
     */
    private BigDecimal glassDensity;

    /**
     * 理论拉引量数据
     */
    private List<DpTheoryPullForceDataVO> data;
}
