package com.steccn.qms.vo;

import com.steccn.qms.dto.QtBaseIdNameDTO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 配料抽检合格率明细表返回数据
 */
@Data
public class QtAccessoryAcceptabilityDetailVO implements Serializable {
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
     * 物料使用车间
     */
    private QtBaseIdNameDTO materialWorkshop;
}
