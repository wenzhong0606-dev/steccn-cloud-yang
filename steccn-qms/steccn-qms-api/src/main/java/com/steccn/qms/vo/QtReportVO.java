package com.steccn.qms.vo;

import com.steccn.qms.dto.QtBaseIdNameDTO;
import lombok.Data;

import java.time.LocalDate;

/**
 * 返回给前端的spc报表控制图数据
 */
@Data
public class QtReportVO {
    /**
     * 表报卡片名称
     */
    private String name;

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
     * 生产车间
     */
    private QtBaseIdNameDTO workshop;

    /**
     * 生产产线
     */
    private QtBaseIdNameDTO productLine;

    /**
     * 客户
     */
    private QtBaseIdNameDTO customer;

    /**
     * 物料使用车间
     */
    private QtBaseIdNameDTO materialUseWorkshop;

    /**
     * 供应商
     */
    private QtBaseIdNameDTO supplier;
}
