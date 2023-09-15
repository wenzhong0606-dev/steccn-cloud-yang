package com.steccn.pms.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 拉引量运算相关DTO
 *
 * @author AndrewLuo
 * @date 2023/09/15
 */
@Data
public class ProcessReportWorkHourQmsDTO implements Serializable {
    /**
     * 产线id
     */
    private List<Integer> productLineIds;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;
}
