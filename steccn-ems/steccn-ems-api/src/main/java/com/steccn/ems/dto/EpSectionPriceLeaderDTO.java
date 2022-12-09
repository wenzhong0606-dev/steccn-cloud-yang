package com.steccn.ems.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 电耗leader
 *
 * @author AndrewLuo
 * @date 2022/12/09
 */
@Data
public class EpSectionPriceLeaderDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "时间段不能为空")
    private LocalDate startDate;
}
