package com.steccn.pms.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author chenhuan
 * @date 2022/11/4 8:51
 */
@Data
public class PmsProducePlanNewExcelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelIgnore
    private Integer rowIndex;

    @ExcelProperty("月份")
    @ApiModelProperty("月份")
    private String month;

    @ExcelProperty("客户名称")
    @ApiModelProperty("客户名称")
    private String customerName;

    @ExcelProperty("客户物料编码")
    @ApiModelProperty("客户物料编码")
    private String customerMaterialCode;

    @ExcelProperty("物料编码")
    @ApiModelProperty("物料编码")
    private String materialCode;

    @ExcelProperty("1")
    @ApiModelProperty("1")
    private BigDecimal one;

    @ExcelProperty("2")
    @ApiModelProperty("2")
    private BigDecimal two;

    @ExcelProperty("3")
    @ApiModelProperty("3")
    private BigDecimal three;

    @ExcelProperty("4")
    @ApiModelProperty("4")
    private BigDecimal four;

    @ExcelProperty("5")
    @ApiModelProperty("5")
    private BigDecimal five;

    @ExcelProperty("6")
    @ApiModelProperty("6")
    private BigDecimal six;

    @ExcelProperty("7")
    @ApiModelProperty("7")
    private BigDecimal seven;

    @ExcelProperty("8")
    @ApiModelProperty("8")
    private BigDecimal eight;

    @ExcelProperty("9")
    @ApiModelProperty("9")
    private BigDecimal nine;

    @ExcelProperty("10")
    @ApiModelProperty("10")
    private BigDecimal ten;

    @ExcelProperty("11")
    @ApiModelProperty("11")
    private BigDecimal eleven;

    @ExcelProperty("12")
    @ApiModelProperty("12")
    private BigDecimal twelve;

    @ExcelProperty("13")
    @ApiModelProperty("13")
    private BigDecimal thirteen;

    @ExcelProperty("14")
    @ApiModelProperty("14")
    private BigDecimal fourteen;

    @ExcelProperty("15")
    @ApiModelProperty("15")
    private BigDecimal fifteen;

    @ExcelProperty("16")
    @ApiModelProperty("16")
    private BigDecimal sixteen;

    @ExcelProperty("17")
    @ApiModelProperty("17")
    private BigDecimal seventeen;

    @ExcelProperty("18")
    @ApiModelProperty("18")
    private BigDecimal eighteen;

    @ExcelProperty("19")
    @ApiModelProperty("19")
    private BigDecimal nineteen;

    @ExcelProperty("20")
    @ApiModelProperty("20")
    private BigDecimal twenty;

    @ExcelProperty("21")
    @ApiModelProperty("21")
    private BigDecimal twentyOne;

    @ExcelProperty("22")
    @ApiModelProperty("22")
    private BigDecimal twentyTwo;

    @ExcelProperty("23")
    @ApiModelProperty("23")
    private BigDecimal twentyThree;

    @ExcelProperty("24")
    @ApiModelProperty("24")
    private BigDecimal twentyFour;

    @ExcelProperty("25")
    @ApiModelProperty("25")
    private BigDecimal twentyFive;

    @ExcelProperty("26")
    @ApiModelProperty("26")
    private BigDecimal twentySix;

    @ExcelProperty("27")
    @ApiModelProperty("27")
    private BigDecimal twentySeven;

    @ExcelProperty("28")
    @ApiModelProperty("28")
    private BigDecimal twentyEight;

    @ExcelProperty("29")
    @ApiModelProperty("29")
    private BigDecimal twentyNine;

    @ExcelProperty("30")
    @ApiModelProperty("30")
    private BigDecimal thirty;

    @ExcelProperty("31")
    @ApiModelProperty("31")
    private BigDecimal thirty0ne;


}
