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
     * 控制图卡片id
     */
    private Integer id;

    /**
     * 报表卡片类型
     */
    private String reportCardType;

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
     * 粒度:小时、天、周、月、季度、年
     */
    private String granularity;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 检验类型
     */
    private Integer checkType;

    /**
     * 检验分组id
     */
    private Integer checkGroupId;

    /**
     * 检验项：为选中检验分组当前快照下所有数值、百分比、自动计算类型的检验项
     */
    private Integer checkItemId;

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
     * 备注
     */
    private String note;

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
