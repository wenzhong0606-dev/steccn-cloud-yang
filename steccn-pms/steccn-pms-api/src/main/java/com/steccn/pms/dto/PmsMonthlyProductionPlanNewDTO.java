package com.steccn.pms.dto;

import com.steccn.common.dto.TupleDTO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@ApiModel("生产月计划-新增/删除")
public class PmsMonthlyProductionPlanNewDTO implements Serializable {

    @NotBlank(message = "月份不能为空")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "月份必须要符合yyyy-MM格式")
    private String date;

    @NotNull(message = "物料不能为空")
    private Long materialId;

    @NotNull(message = "客户不能为空")
    private Integer clientId;

    /**
     * 日期属性
     */
    private List<TupleDTO<LocalDate, BigDecimal>> value;

    /**
     * 下达
     */
    private Long linkId;

    private static final long serialVersionUID = 1L;

}
