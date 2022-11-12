package com.steccn.qms.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class QtAccessoryAcceptabilityDTO {
    /**
     * 检验完成时间在选中日期8:00 到20:00内的所有配料抽检任务
     */
    private LocalDate dateTime;

    /**
     * 对比
     */
    private String compare;

    /**
     * 物料使用车间
     */
    private Integer productWorkShopId;

    /**
     * 区分柱状图饼状图的字段
     */
    private Integer type;

}
