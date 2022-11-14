package com.steccn.qms.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class QtIQCAcceptabilityDTO extends BasePageDTO implements Serializable {
    /**
     * 日期
     */
    private LocalDateTime dateTime;

    /**
     * 对比
     */
    private Integer compare;

    /**
     * 筛选的取样时间
     */
    private LocalDateTime samplingTime;

    /**
     * 筛选的物料
     */
    private Integer materialId;

    /**
     * 筛选物料使用车间
     */
   private Integer materialWorkshopId;

    /**
     * 筛选供应商
     */
    private Integer supplierId;
}
