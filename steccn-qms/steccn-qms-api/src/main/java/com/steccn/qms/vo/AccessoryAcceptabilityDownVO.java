package com.steccn.qms.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenhuan
 * @date 2022/11/18 16:21
 */
@Data
public class AccessoryAcceptabilityDownVO {
    /**
     * 检验任务单号
     */
    private String code;

    /**
     * 合格率
     */
    private String acceptability;

    /**
     * 检验结果
     */
    private String result;

    /**
     * 检验完成时间
     */
    private String finishedTime;

    private String materialWorkshopName;
}
