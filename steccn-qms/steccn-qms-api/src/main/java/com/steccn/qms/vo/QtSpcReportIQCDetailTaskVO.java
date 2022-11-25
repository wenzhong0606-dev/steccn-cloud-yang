package com.steccn.qms.vo;

import com.steccn.qms.dto.QtBaseIdNameDTO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;
@Data
public class QtSpcReportIQCDetailTaskVO implements Serializable {
    /**
     * 检验任务单号的id
     */
    private Long id;

    /**
     * 检验任务单号
     */
    private String code;

    /**
     * 检验完成时间
     */
    private String finishedTime;

    /**
     * 检验结果
     */
    private String checkResult;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 物料使用车间
     */
    private  QtBaseIdNameDTO workshop;

    /**
     * 供应商
     */
    private QtBaseIdNameDTO supplier;
}
