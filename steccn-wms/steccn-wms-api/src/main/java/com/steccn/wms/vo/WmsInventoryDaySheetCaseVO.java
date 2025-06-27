package com.steccn.wms.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ApiModel("入库统计日报表箱列表VO")
public class WmsInventoryDaySheetCaseVO {

    private String shelfName;

    private String materialName;

    private BigDecimal quantity;

    private String caseNo;

    private String code;

    private Integer status;

    private LocalDateTime createTime;

    private String createName;

    private LocalDateTime affirmTime;

    private String affirmName;

    private LocalDateTime comeTime;

    private String productionLineName;

    private LocalDateTime outTime;

    private String targetShelfName;

    private Integer targetShelfId;

    private Integer warehouseId;

    private String warehouseName;


}
