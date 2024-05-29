package com.steccn.wms.vo;


import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDate;

@Data
@ApiModel("销售物流单")
public class SalesLogisticsBillVO {
    private LocalDate sendDate;
    private String code;
    private Integer pieceQty;
    private Long typeId;
    private String type;
}
