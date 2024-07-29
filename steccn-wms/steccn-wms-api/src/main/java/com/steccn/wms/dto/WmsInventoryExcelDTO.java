package com.steccn.wms.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.steccn.platform.vo.EqpSpareVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
public class WmsInventoryExcelDTO  implements Serializable {


    /**
     * 物料id
     */
    @ApiModelProperty("物料主键")
    @ExcelIgnore
    private Long materialId;

    /**
     * 物料名称
     */
    @ApiModelProperty("物料名称")
    @ExcelProperty("物料名称")
    private String materialName;

    /**
     * 物料编码
     */
    @ApiModelProperty("物料编码")
    @ExcelProperty("物料编码")
    private String materialCode;

    /**
     * 仓库id
     */
    @ApiModelProperty("仓库id")
    @ExcelIgnore
    private Long warehouseId;


    /**
     * 仓库名称
     */
    @ApiModelProperty("仓库名称")
    @ExcelProperty("仓库名称")
    private String warehouseName;

    @ApiModelProperty("仓库编码")
    @ExcelIgnore
    private String warehouseCode;


    /**
     * 货架id
     */
    @ApiModelProperty("货架id")
    @ExcelIgnore
    private Long shelfId;

    /**
     * 货架名称
     */
    @ApiModelProperty("货架名称")
    @ExcelProperty("货架名称")
    private String shelfName;


    @ApiModelProperty("货架编码")
    @ExcelProperty("货架编码")
    private String shelfCode;

    /**
     * 总数
     */
    @ApiModelProperty("总数")
    @ExcelProperty("总数")
    private BigDecimal totalQuantity;

    /**
     * 可用数量
     */
    @ApiModelProperty("可用数量")
    @ExcelProperty("可用数量")
    private BigDecimal availableQuantity;

    /**
     * 在途库存
     */
    @ApiModelProperty("在途库存")
    @ExcelProperty("在途库存")
    private BigDecimal stockQuantity;

    /**
     * 冻结库存
     */
    @ApiModelProperty("冻结库存")
    @ExcelProperty("冻结库存")
    private BigDecimal freezeQuantity;

    /**
     * 锁定库存
     */
    @ApiModelProperty("锁定库存")
    @ExcelProperty("锁定库存")
    private BigDecimal lockQuantity;

    /**
     * 质检库存
     */
    @ApiModelProperty("质检库存")
    @ExcelProperty("质检库存")
    private BigDecimal detectionQuantity;

    /**
     * 单位id
     */
    @ApiModelProperty("单位id")
    @ExcelIgnore
    private Long unit;

    /**
     * 单位名称
     */
    @ApiModelProperty("单位名称")
    @ExcelProperty("单位名称")
    private String unitName;

    /**
     * 箱号
     */
    @ApiModelProperty("箱号")
    @ExcelProperty("箱号")
    private String caseNo;

    /**
     * 批次
     */
    @ApiModelProperty("批次")
    @ExcelProperty("批次")
    private String batch;

    /**
     * 打包时间
     */
    @ApiModelProperty("打包时间")
    @ExcelProperty("打包时间")
    @ExcelIgnore
    private LocalDateTime packTime;


    @ApiModelProperty("物料规格")
    @ExcelProperty("物料规格")
    @ExcelIgnore
    private String specifications;

}
