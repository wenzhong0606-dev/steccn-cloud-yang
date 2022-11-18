package com.steccn.qms.vo;

import com.steccn.qms.dto.QtBaseIdNameDTO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * spc图表明细返回数据对象
 */
@Data
public class QtSpcReportDetailTaskVO implements Serializable {
    /**
     * 单号的id
     */
    private Long id;

    /**
     * 检验任务单号
     */
    private String code;

    /**
     * 任务类型 来料检1/首检2/在线检3/巡检4/出货检5/化验抽检6/配料抽检7
     */
    private Integer taskType;

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
     * 生产产线
     */
    private QtBaseIdNameDTO productLine;

    /**
     * 客户
     */
    private QtBaseIdNameDTO customer;
}
