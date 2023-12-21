package com.steccn.pms.dto;

import com.steccn.common.dto.TupleDTO;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 原料配料累加DTO
 *
 * @author AndrewLuo
 * @date 2023/12/20
 */
@Data
public class PmsRawMaterialCountDTO implements Serializable {
    @NotNull(message = "日期不能为空")
    private LocalDate date;

    @NotEmpty(message = "数据不能为空")
    private List<TupleDTO<String, BigDecimal>> data;
}
