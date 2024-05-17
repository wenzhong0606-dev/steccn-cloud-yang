package com.steccn.wms.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.w3c.dom.stylesheets.LinkStyle;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel("仓储统计日报表VO")
public class WmsInventoryDaySheetVO {

    private String materialName;

    private Long type;

    private String typeName;

    private Integer warehouseId;

    private String warehouseName;

    private String code;

    private String productionLineName;

    private String status;

    private Integer caseNum;

    private BigDecimal totalQuantity;

    private String classes;

    private String caseNo;

    private List<String> caseNoList;


}
