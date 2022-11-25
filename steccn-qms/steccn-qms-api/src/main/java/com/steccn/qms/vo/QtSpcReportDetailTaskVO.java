package com.steccn.qms.vo;

import com.steccn.qms.dto.QtBaseIdNameDTO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * spc明细表的子集对象 (任务详情)
 */
@Data
public class QtSpcReportDetailTaskVO implements Serializable {
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
    private QtBaseIdNameDTO workshop;

    /**
     * 供应商
     */
    private QtBaseIdNameDTO supplier;

}
