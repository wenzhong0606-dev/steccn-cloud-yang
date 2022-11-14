package com.steccn.qms.vo;

import com.steccn.qms.dto.QtBaseIdNameDTO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QtIQCAcceptabilityDetailVO implements Serializable {
    /**
     * 检验任务单号
     */
    private String code;

    /**
     * 合格率
     */
    private BigDecimal acceptability;

    /**
     * 检验结果
     */
    private String result;

    /**
     * 检验完成时间
     */
    private String finishedTime;

    /**
     * 取样时间
     */
    private LocalDateTime samplingTime;

    /**
     * 物料使用车间
     */
    private QtBaseIdNameDTO materialWorkshop;

    /**
     * 物料
     */
    private Long materialId;

    /**
     * 供应商
     */
    private QtBaseIdNameDTO supplier;
}
