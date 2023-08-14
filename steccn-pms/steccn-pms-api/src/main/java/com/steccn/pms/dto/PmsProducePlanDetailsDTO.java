package com.steccn.pms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author chenhuan
 * @date 2022/11/3 14:16
 */
@Data
public class PmsProducePlanDetailsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "计划日期不能为空")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate planDate;
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "数量不能为空")
    private BigDecimal quantity;
    private Integer customerId;
    private String customerCode;
    private String customerName;
}
