package com.steccn.oa.vo;

import com.steccn.common.vo.QtChartPieVO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * oa出勤数据
 *
 * @author AndrewLuo
 * @date 2023/04/18
 */
@Data
public class OaAttendanceVO implements Serializable {
    /**
     * 应出勤天数
     */
    private BigDecimal requiredNum;
    /**
     * 实际出勤天数
     */
    private BigDecimal actualNum;
    /**
     * 未出勤天数
     */
    private BigDecimal unAttendedNum;
    /**
     * 迟到次数
     */
    private BigDecimal lateNum;
    /**
     * 早退次数
     */
    private BigDecimal earlyNum;
    /**
     * 旷工次数
     */
    private BigDecimal absentNum;
    /**
     * 漏签次数
     */
    private BigDecimal missedNum;
    /**
     * 图表
     */
    private QtChartPieVO chart;
}
