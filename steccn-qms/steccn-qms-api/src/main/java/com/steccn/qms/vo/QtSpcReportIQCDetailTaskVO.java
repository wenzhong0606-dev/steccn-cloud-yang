package com.steccn.qms.vo;

import com.steccn.qms.dto.QtBaseIdNameDTO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;
@Data
public class QtSpcReportIQCDetailTaskVO implements Serializable {
    /**
     * 检验任务单号
     */
    private Map<String,String> code;

    /**
     * 检验完成时间
     */
    private Map<String,String> finishedTime;

    /**
     * 检验结果
     */
    private Map<String, BigDecimal> checkResult;

    /**
     * 物料名称
     */
    private Map<String,String> materialName;

    /**
     * 物料使用车间
     */
    private Map<String, QtBaseIdNameDTO> workshop;

    /**
     * 供应商
     */
    private Map<String,QtBaseIdNameDTO> supplier;
}
