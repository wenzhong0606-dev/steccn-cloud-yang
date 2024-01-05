package com.steccn.pms.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RequisitionInfoDTO {


    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("需求数量")
    private BigDecimal quantity;

    @ApiModelProperty("领用数量")
    private BigDecimal receiveQuantity;

    @ApiModelProperty("需求时间")
    private LocalDate requireDate;
}
