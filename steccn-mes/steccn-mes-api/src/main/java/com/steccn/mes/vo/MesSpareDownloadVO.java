package com.steccn.mes.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 备品备件下载VO
 *
 * @author AndrewLuo
 * @date 2023/08/04
 */
@Data
public class MesSpareDownloadVO {
    /**
     * ID
     */
    @ExcelProperty("ID")
    private String id;

    /**
     * 编码
     */
    @ExcelProperty("编码")
    private String code;

    /**
     * 名称
     */
    @ExcelProperty("名称")
    private String name;

    /**
     * 规格型号
     */
    @ExcelProperty("规格型号")
    private String specifications;

    /**
     * 长度单位
     */
    @ExcelProperty("长度单位")
    private Long lengthUnitId;

    /**
     * 长度
     */
    @ExcelProperty("长度")
    private BigDecimal lengthQuantity;

    /**
     * 高度单位
     */
    @ExcelProperty("高度单位")
    private Long highUnitId;

    /**
     * 高度
     */
    @ExcelProperty("高度")
    private BigDecimal highQuantity;

    /**
     * 宽度单位
     */
    @ExcelProperty("宽度单位")
    private Long wideUnitId;

    /**
     * 宽度
     */
    @ExcelProperty("宽度")
    private BigDecimal wideQuantity;

    /**
     * 采购类型id
     */
    @ExcelProperty("采购类型")
    private Long procurementTypeId;

    /**
     * 体积
     */
    @ExcelProperty("体积")
    private BigDecimal volumeQuantity;

    /**
     * 体积单位
     */
    @ExcelProperty("体积单位")
    private Long volumeUnitId;

    /**
     * 重量
     */
    @ExcelProperty("重量")
    private BigDecimal weightQuantity;

    /**
     * 重量单位
     */
    @ExcelProperty("重量单位")
    private Long weightUnitId;

    /**
     * 单位
     */
    @ExcelProperty("单位")
    private String unit;

    /**
     * 厚度
     */
    @ExcelProperty("厚度")
    private BigDecimal thickQuantity;

    /**
     * 厚度单位
     */
    @ExcelProperty("厚度单位")
    private Long thickUnitId;

    /**
     * 状态（0正常 1删除 2停用）
     */
    @ExcelProperty("状态")
    private String status;
    /**
     * 类型
     */
    @ExcelProperty("类型")
    private String type;

    /**
     * 业务状态
     */
    @ExcelProperty("业务状态")
    private String state;

    /**
     * 批次类型
     */
    @ExcelProperty("批次类型")
    private String batchType;

    /**
     * 备注
     */
    @ExcelProperty("备注")
    private String remarks;

    /**
     * 扩展编码
     */
    @ExcelProperty("扩展编码")
    private String extendCode;

    /**
     *
     */
    @ExcelProperty("数量")
    private BigDecimal quantity;

    /**
     * 单位id
     */
    @ExcelProperty("单位id")
    private Long unitId;
}
