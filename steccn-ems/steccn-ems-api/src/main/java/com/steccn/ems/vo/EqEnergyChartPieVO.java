package com.steccn.ems.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 峰谷平电耗分布饼状图DTO
 *
 * @author AndrewLuo
 * @date 2022/12/06
 */
@Data
public class EqEnergyChartPieVO<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 饼状图
     */
    private List<EpEnergyChartPieValueVO<T>> data;
}
