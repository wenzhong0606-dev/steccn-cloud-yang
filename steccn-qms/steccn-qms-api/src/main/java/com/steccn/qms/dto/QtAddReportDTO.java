package com.steccn.qms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class QtAddReportDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 报表卡片类型
     */
    @NotEmpty(message="报表卡片类型不能为空",groups = {InsertGroup.class, UpdateGroup.class})
    private String reportCardType;

    /**
     * 表报卡片名称
     */
    @NotEmpty(message="报表卡片名称不能为空",groups = {InsertGroup.class, UpdateGroup.class})
    private String name;

    /**
     * 日期范围开始时间
     */
    @NotNull(message = "日期范围开始时间不能为空",groups = {InsertGroup.class, UpdateGroup.class})
    private LocalDate timeScopeStart;

    /**
     * 日期范围结束时间
     */
    @NotNull(message = "日期范围结束时间不能为空",groups = {InsertGroup.class, UpdateGroup.class})
    private LocalDate timeScopeEnd;

    /**
     * 粒度:小时、天、周、月、季度、年
     */
    @NotNull(message = "粒度不能为空",groups = {InsertGroup.class, UpdateGroup.class})
    private String granularity;

    /**
     * 物料名称
     */
    @NotNull(message = "物料名称id不能为空",groups = {InsertGroup.class, UpdateGroup.class})
    private Long materialId;

    /**
     * 检验类型:来料检1/首检2/在线检3/巡检4/出货检5/化验抽检6/配料抽检7
     */
    @NotNull(message = "检验类型不能为空",groups = {InsertGroup.class, UpdateGroup.class})
    private Integer checkType;

    /**
     * 检验分组
     */
    @NotNull(message = "检验分组不能为空",groups = {InsertGroup.class, UpdateGroup.class})
    private Integer checkGroupId;

    /**
     * 检验项：为选中检验分组当前快照下所有数值、百分比、自动计算类型的检验项
     */
    @NotNull(message = "检验项id不能为空",groups = {InsertGroup.class, UpdateGroup.class})
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
    private BigDecimal dowmLimit;

    /**
     * 生产车间
     */
    private QtBaseIdNameDTO workshop;

    /**
     * 产线产生
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

    /**
     * 备注
     */
    private String note;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 更新人id
     */
    private Long updateBy;

    /**
     * 更新人名称
     */
    private String updateName;

}
