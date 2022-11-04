package com.steccn.pms.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author chenhuan
 * @date 2022/11/4 8:51
 */
@Data
public class PmsProducePlanExcelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelIgnore
    private Integer rowIndex;

    @ExcelProperty("物料编码")
    @ApiModelProperty("物料编码")
    private String materialCode;

    @ExcelProperty("物料名称")
    @ApiModelProperty("物料名称")
    private String materialName;

    @ExcelProperty("日期")
    @ApiModelProperty("日期")
    private String planDate;

    @ExcelProperty("数量")
    @ApiModelProperty("数量")
    private BigDecimal quantity;

}
