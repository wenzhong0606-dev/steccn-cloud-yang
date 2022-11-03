package com.steccn.pms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

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

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate planDate;
    private BigDecimal quantity;
}
