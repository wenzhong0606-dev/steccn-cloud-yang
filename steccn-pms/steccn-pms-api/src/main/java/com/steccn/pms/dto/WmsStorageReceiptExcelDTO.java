package com.steccn.pms.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2023/1/6
 */
@Data
public class WmsStorageReceiptExcelDTO {

    @ExcelProperty("入库类型")
    private String type;

    @ExcelProperty("仓库名称")
    private String warehouseName;

    @ExcelProperty("物料编码")
    private String materialCode;

    @ExcelProperty("物料名称")
    private String materialName;

    @ExcelProperty("规格型号")
    private String specifications;

    @ExcelProperty("货架")
    private String shelfName;

    @ExcelProperty("箱号")
    private String caseNo;

    @ExcelProperty("批号")
    private String batchNo;

    @ExcelProperty("库存主单位")
    private String unitName;

    @ExcelProperty("库存量")
    private BigDecimal quantity;
}
