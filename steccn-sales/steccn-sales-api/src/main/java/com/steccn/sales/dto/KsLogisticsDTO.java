package com.steccn.sales.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 仓库
 *
 * @author AndrewLuo
 * @date 2023/11/29
 */
@Data
public class KsLogisticsDTO extends BasePageDTO implements Serializable {
    /**
     * 仓库名称
     */
    @NotEmpty(message = "仓库名称不能为空")
    private String warehouseName;
    /**
     * 日期
     */
    @NotNull(message = "时间不能为空")
    private LocalDate date;
}
