package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 深加工或基片合格率明细表的检验任务详情
 */
@Data
public class QtSubOrDeepFPYTaskDetailsVO implements Serializable {
    /**
     * 检验任务单号
     */
    private String code;

    /**
     * 检验完成时间
     */
    private LocalDateTime finishedTime;

    /**
     * 检验结果
     */
    private String result;

    /**
     * 检验详情合格率
     */
    private BigDecimal detailAcceptability;
}
