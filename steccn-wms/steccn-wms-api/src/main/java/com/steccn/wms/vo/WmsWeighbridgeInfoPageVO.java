package com.steccn.wms.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2024/07/17
 */
public class WmsWeighbridgeInfoPageVO<T> extends Page <T> {
    /**
     * 毛重总数
     */
    private BigDecimal grossWeightCount;

    /**
     * 净重总数
     */
    private BigDecimal netWeightCount;

    public BigDecimal getGrossWeightCount() {
        return grossWeightCount;
    }

    public void setGrossWeightCount(BigDecimal grossWeightCount) {
        this.grossWeightCount = grossWeightCount;
    }

    public BigDecimal getNetWeightCount() {
        return netWeightCount;
    }

    public void setNetWeightCount(BigDecimal netWeightCount) {
        this.netWeightCount = netWeightCount;
    }
}
