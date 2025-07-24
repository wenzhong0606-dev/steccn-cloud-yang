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

    private Long materialId;

    private String materialCode;

    private Long type;

    private String typeName;

    private Integer warehouseId;

    private Integer sort;

    private String warehouseName;

    private Integer targetWarehouseId;

    private String targetWarehouseName;

    private String code;

    private String productionLineName;

    private String productionLineCode;

    private Integer productionLineId;

    private Integer targetShelfId;

    private String targetShelfName;

    private Integer sourceShelfId;

    private String sourceShelfName;

    private String status;

    private Integer caseNum;

    private BigDecimal totalQuantity;

    private String classes;

    private String caseNo;

    private List<String> caseNoList;

    private List<String> receiptCode;

    private List<String> codeList;

}
