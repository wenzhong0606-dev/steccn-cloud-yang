package com.steccn.pms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author yequan
 * @desc 深加工报表停机行对象
 * @date 2023/12/27
 */
@Data
public class SjgCountReportStopRowVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 工序名称
     */
    private String processName;

    /**
     * 工位主键
     */
    private Integer stationId;

    /**
     * 班次日期
     */
    private LocalDate classDate;

    /**
     * 影响原因-备注
     */
    private String note;

    /**
     * 时长 ， 单位小时
     */
    private BigDecimal manHour;

    /**
     * 产线基数
     */
    private Integer lineNum;

    /**
     * 影响产量
     */
    private BigDecimal effectQuantity;


}
