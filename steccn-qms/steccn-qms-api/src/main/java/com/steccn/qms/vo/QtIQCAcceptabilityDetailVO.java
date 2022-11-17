package com.steccn.qms.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.steccn.qms.dto.QtBaseIdNameDTO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QtIQCAcceptabilityDetailVO implements Serializable {

    /**
     * 主键
     *
     */
    private Long id;
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
    private LocalDate finishedTime;


    /**
     * 取样时间
     */
    private LocalDate samplingTime;

    /**
     * 物料使用车间
     */
    private QtBaseIdNameDTO materialWorkshop;

    /**
     * 物料
     */
    private Long materialId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 供应商
     */
    private QtBaseIdNameDTO supplier;

    /**
     * 生产车间的id
     */
    private QtBaseIdNameDTO productWorkshop;

    /**
     * 检验数量
     */
    @JsonIgnore
    private Integer checkNum;

    /**
     * 合格数量
     */
    @JsonIgnore
    private Integer goodNum;

    /**
     * 来料检合格率明细表样本明细
     */
    private List<QtIQCAcceptabilityCopyVO> detail;
}
