package com.steccn.pms.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

/**
 * @desc 拉引量汇总dto
 */
@Data
public class PullForceSumDTO implements Serializable {

    private final static long serialVersionUID = 1L;

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
     * 车间集合
     */
    @NotNull(message = "车间集合不为空")
    private Set<Integer> workshopIdSet;

    /**
     * 粒度
     */
    @NotNull(message = "粒度不为空")
    private Integer granularity;
}
