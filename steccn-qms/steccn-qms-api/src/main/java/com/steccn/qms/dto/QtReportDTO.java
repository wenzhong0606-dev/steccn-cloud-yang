package com.steccn.qms.dto;

import com.google.common.collect.Lists;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QtReportDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键id
     */
    @NotNull(groups = {UpdateGroup.class},message = "编辑卡片类型时id不能为空")
    private Integer id;

    /**
     * 报表卡片类型
     */
    @NotBlank(message="报表卡片类型不能为空",groups = {InsertGroup.class, UpdateGroup.class})
    private String reportCardType;

    /**
     * 表报卡片名称
     */
    @NotBlank(groups = {InsertGroup.class, UpdateGroup.class}, message = "名称不能为空")
    @Length(groups = {InsertGroup.class, UpdateGroup.class}, max = 20, message = "名称长度不超过20个中文")
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
    @NotBlank(message = "粒度不能为空",groups = {InsertGroup.class, UpdateGroup.class})
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

    /**
     * 备注
     */
    @Length(groups = {InsertGroup.class, UpdateGroup.class}, max = 100, message = "备注长度不超过100个中文")
    private String note;

    /**
     * 报表卡片类型id
     */
    @NotNull(message = "报表卡片类型分组id不能为空",groups = {InsertGroup.class})
    private Integer reportGroupId;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    private String createName;

    private LocalDateTime creatTime;
    /**
     * 更新人id
     */
    private Long updateBy;

    /**
     * 更新人名称
     */
    private String updateName;

}
