package com.steccn.ems.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 能源流向DTO
 *
 * @author AndrewLuo
 * @date 2022/12/12
 */
@Data
public class EpEnergyFlowDTO extends BasePageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    private LocalDate dateStart;

    /**
     * 结束日期
     */
    @NotNull(message = "结束日期不能为空")
    private LocalDate dateEnd;

    /**
     * 能源类型
     */
    @NotNull(message = "能源类型不能为空")
    private Integer energyTypeId;
}
