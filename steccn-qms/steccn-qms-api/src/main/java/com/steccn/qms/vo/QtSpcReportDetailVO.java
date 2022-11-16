package com.steccn.qms.vo;

import com.steccn.qms.dto.QtBaseIdNameDTO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QtSpcReportDetailVO implements Serializable {

    /**
     * 时间分片开始时间
     */
    private LocalDateTime startTime;

    /**
     * 时间分片结束时间
     */
    private LocalDateTime endTime;


    /**
     * 检验项名称
     */
    private String  checkItemName;

    /**
     * 计量单位
     */
    private String measureUnit;

    /**
     * 检验结果
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
     * 每个检验项下面的检验任务根据分片时间分好片的任务详情
     */
   private List<QtSpcReportDetailTaskVO> reportDetailTaskVOList;


}
