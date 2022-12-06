package com.steccn.ems.vo;

import lombok.Data;

/**
 * 饼状图数据VO
 *
 * @author AndrewLuo
 * @date 2022/12/06
 */
@Data
public class EpEnergyChartPieValueVO<T> {
    /**
     * 数值
     */
    private T value;

    /**
     * 名字
     */
    private String name;

}
