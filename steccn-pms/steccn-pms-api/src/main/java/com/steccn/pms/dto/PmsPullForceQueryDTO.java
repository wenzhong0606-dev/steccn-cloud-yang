package com.steccn.pms.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

/**
 * 拉引量查询DTO
 *
 * @author AndrewLuo
 * @date 2023/07/07
 */
@Data
public class PmsPullForceQueryDTO implements Serializable {
    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    private LocalDate startDate;
    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    private LocalDate endDate;
    /**
     * 产线id
     */
    private Set<Integer> productLineIds;
}
