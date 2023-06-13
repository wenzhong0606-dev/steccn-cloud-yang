package com.steccn.qms.vo;

import com.steccn.qms.dto.QtBaseIdNameDTO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 返回给前端的spc报表控制图数据
 */
@Data
public class QtReportVO implements Serializable {
    /**
     * 报表卡片id
     */
    private Integer id;
    /**
     * 报表卡片类型
     */
    private String reportCardType;
    /**
     * 表报卡片名称
     */
    private String name;

    /**
     * 动态日期类型
     */
    private Integer dynamicDateType;
    /**
     * 日期范围开始时间
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
     * 物料名称
     */
    private Long materialId;

    /**
     * 检验类型:来料检1/首检2/在线检3/巡检4/出货检5/化验抽检6/配料抽检7
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
}
