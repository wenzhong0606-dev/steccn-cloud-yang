package com.steccn.wms.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/12/9
 */
@Data
@ApiModel
public class WmsDeliveryReceiptInfoVO {

    @ApiModelProperty("主键")
    @ExcelIgnore
    private Long id;

    @ApiModelProperty("库存id")
    @ExcelIgnore
    private Long inventoryId;

    @ApiModelProperty("物料id")
    @ExcelIgnore
    private Long materialId;

    @ApiModelProperty("物料编码")
    @ExcelProperty("物料编码")
    private String materialCode;

    @ApiModelProperty("物料名称")
    @ExcelProperty("物料名称")
    private String materialName;

    @ApiModelProperty("货架id")
    @ExcelIgnore
    private Long shelfId;

    @ApiModelProperty("货架名称")
    @ExcelProperty("货架名称")
    private String shelfName;

    @ApiModelProperty("数量")
    @ExcelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("关联单据")
    @ExcelProperty("关联单据")
    private String associatedNo;

    @ApiModelProperty("批次")
    @ExcelProperty("批次")
    private String batch;

    @ApiModelProperty("箱号")
    @ExcelProperty("箱号")
    private String caseNo;

    @ApiModelProperty("单位")
    @ExcelIgnore
    private Long unit;

    @ApiModelProperty("单位名称")
    @ExcelProperty("单位")
    private String unitName;

    @ApiModelProperty("确认时间")
    @ExcelIgnore
    private LocalDateTime affirmTime;

    @ExcelProperty("规格")
    private String specification;
    @ExcelIgnore
    private Integer count;

}
