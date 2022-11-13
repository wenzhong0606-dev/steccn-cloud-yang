package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
public class QtSubLaboratoryAcceptabilityDTO implements Serializable {
    /**
     * 开始时间
     */
    private LocalDate startTime;

    /**
     * 结束时间
     */
    private LocalDate endTime;

    /**
     * 对比
     */
    private Integer compare;

    /**
     * 筛选基片生产车间的id  75:基片一车间 ,76:基片二车间
     */
    private Integer productWorkshopId;

    /**
     * type:柱状图0  饼图1
     */
    private Integer type;
}
