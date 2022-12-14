package com.steccn.ems.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 能源流向领导驾驶舱
 *
 * @author AndrewLuo
 * @date 2022/12/14
 */
@Data
public class EpEnergyFlowLeaderDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "日期不能为空")
    private LocalDate dateStart;
}
