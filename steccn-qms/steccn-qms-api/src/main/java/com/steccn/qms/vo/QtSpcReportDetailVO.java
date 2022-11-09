package com.steccn.qms.vo;

import com.steccn.qms.dto.QtBaseIdNameDTO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class QtSpcReportDetailVO implements Serializable {
    /**
     * 主键id
     */
     private Integer id;

    /**
     * 报表卡片名称
     */
    private String name;
    
    /**
     * 开始时间
     */
    private LocalDate timeScopeStart;

    /**
     * 结束时间
     */
    private LocalDate timeScopeEnd;

    /**
     * 检验项id
     */
    private Integer checkItemId;

    /**
     * 检验项名称
     */
    private String  checkItemName;

    /**
     * 计量单位
     */
    private Long measureUnit;

    /**
     * 检验结果平均值
     */
    private BigDecimal checkItemResultAverage;

    /**
     * 标准值
     */
    private BigDecimal standardValue;

    /**
     * 上限
     */
    private BigDecimal upLimit;

    /**
     * 下限
     */
    private BigDecimal downLimit;

    /**
     * 检验任务单号
     */
    private Integer checkTaskId;

    /**
     * 检验完成时间
     */
    private LocalDate finishedTime;

    /**
     * 检验结果
     */
    private Integer result;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 生产产线
     */
    private List<QtBaseIdNameDTO> productLine;

    /**
     * 客户
     */
    private List<QtBaseIdNameDTO> customer;


}
