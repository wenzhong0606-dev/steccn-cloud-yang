package com.steccn.qms.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * SPC报表excel导入
 */
@Data
public class QtReportExcelDTO implements Serializable {
    @ExcelProperty(value = "图表路径")
    private MultipartFile picture;

    /**
     * 开始时间
     */
    private LocalDate timeScopeStart;

    /**
     * 结束时间
     */
    private LocalDate timeScopeEnd;

    /**
     * 检验项
     */
    private String  checkItemName;

    /**
     * 计量单位
     */
    private String measureUnit;


    /**
     * 标准值
     */
    private BigDecimal standardValue;

    /**
     * 上限
     */
    private BigDecimal upLimit;

    /**
     * 下限
     */
    private BigDecimal downLimit;

    /**
     * 检验任务单号
     */
    private Integer checkTaskId;

    /**
     * 检验完成时间
     */
    private LocalDate finishedTime;

    /**
     * 检验结果
     */
    private Integer result;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 生产产线
     */
    private List<QtBaseIdNameDTO> productLine;

    /**
     * 客户
     */
    private List<QtBaseIdNameDTO> customer;

}
