package com.steccn.qms.vo;

import com.steccn.qms.dto.QtBaseIdNameDTO;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
@Data
public class QtReportChartVO implements Serializable {
    /**
     * 控制图卡片名称
     */
    private String name;

    /**
     * 开始时间
     */
    private LocalDate timeScopeStart;
    /**
     * 日期范围结束时间
     */
    private LocalDate timeScopeEnd;

    /**
     * 筛选条件
     */
   private List<String> spcFilter;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 检验类型
     */
    private Integer checkType;


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
     * 检验项名称
     */
    private String  checkItemName;
    /**
     * 生产车间
     */
    private List<QtBaseIdNameDTO> workshop;

    /**
     * 生产产线
     */
    private List<QtBaseIdNameDTO> productLine;

    /**
     * 客户
     */
    private List<QtBaseIdNameDTO> customer;

    /**
     * 物料使用车间
     */
    private List<QtBaseIdNameDTO> materialUseWorkshop;

    /**
     * 供应商
     */
    private List<QtBaseIdNameDTO> supplier;

    /**
     * 计量单位
     */
    private String measureUnit;

    /**
     * 横坐标数据
     */
    private List<String> xData;

    /**
     * 检验值
     */
    private List<String> itemValue;
}
