package com.steccn.ems.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

/**
 * @author yequan
 * @desc 碳排放报表请求类
 */
@Data
public class CeCarbonReportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不为空")
    private LocalDate start;

    /**
     * 结束日期
     */
    @NotNull(message = "结束日期不为空")
    private LocalDate end;

    /**
     * 粒度
     */
    @NotNull(message = "粒度不为空")
    private Integer granularity;
}
