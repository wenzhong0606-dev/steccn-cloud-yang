package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.steccn.qms.dto.QtBaseIdNameDTO;
import lombok.Data;

/**
 * 巡检任务表
 * @TableName qt_check_task
 */
@TableName(value ="qt_check_task" ,autoResultMap = true)
@Data
public class QtCheckTask implements Serializable {
    /**
     * 主键【数据量大】
     */
    @TableId
    private Long id;

    /**
     * 任务类型 0原始任务  1复检任务
     */
    private Integer type;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 物料车间
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private QtBaseIdNameDTO materialWorkshop;

    @TableField(value = "material_workshop ->> '$.id'",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER,
            select = true)
    private Integer materialWorkshopId;

    @TableField(value = "material_workshop ->> '$.name'",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER,
            select = true)
    private String materialWorkshopName;

    /**
     * 生产车间
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private QtBaseIdNameDTO productWorkshop;

    @TableField(value = "product_workshop ->> '$.name'",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER,
            select = true)
    private String productWorkshopName;

    @TableField(value = "product_workshop ->> '$.id'",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER,
            select = true)
    private Integer productWorkshopId;

    /**
     * 产线
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private QtBaseIdNameDTO productLine;

    @TableField(value = "product_line ->> '$.name'",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER,
            select = true)
    private String productLineName;

    /**
     * 产线id
     */
    @TableField(value = "product_line ->> '$.id'",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER,
            select = true)
    private Integer productLineId;

    /**
     * 生产工单
     */
    private String productOrder;

    /**
     * 生产批次
     */
    private String productBatch;

    /**
     * 检验方案id
     */
    private Integer checkPlanId;

    /**
     * 任务状态 0待检验 1检验中 2已检验 3已取消 4暂存
     */
    private Integer status;

    /**
     * 检验数量
     */
    private Integer checkNum;

    /**
     * 合格数量
     */
    private Integer goodNum;

    /**
     * 不合格数量
     */
    private Integer badNum;

    /**
     * 结果判定
     */
    private String result;

    /**
     * 备注
     */
    private String note;

    /**
     * 任务开始时间
     */
    private LocalDateTime startTime;

    /**
     * 任务完成时间
     */
    private LocalDateTime finishedTime;

    /**
     * 到货时间
     */
    private LocalDate arrivalTime;

    /**
     * 发货时间
     */
    private LocalDate deliverTime;

    /**
     * 供应商id
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private QtBaseIdNameDTO supplier;

    @TableField(value = "supplier ->> '$.name'",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER,
            select = true)
    private String supplierName;

    @TableField(value = "supplier ->> '$.id'",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER,
            select = true)
    private Integer supplierId;

    /**
     * 客户id
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private QtBaseIdNameDTO customer;

    @TableField(value = "customer ->> '$.name'",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER,
            select = true)
    private String customerName;

    @TableField(value = "customer ->> '$.id'",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER,
            select = true)
    private Integer customerId;

    /**
     * 缺陷总数
     */
    private Integer defectNum;

    /**
     * 缺陷记录开始时间
     */
    private LocalDateTime defectStartTime;

    /**
     * 缺陷记录结束时间
     */
    private LocalDateTime defectEndTime;

    /**
     * 任务类型 来料检1/首检2/在线检3/巡检4/出货检5/化验抽检6/配料抽检7
     */
    private Integer taskType;

    /**
     * 发货数量
     */
    private Integer deliverNum;

    /**
     * 发货单号
     */
    private String deliverOrder;

    /**
     * 任务单号
     */
    private String code;

    /**
     * 创建人id
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建人名称
     */
    @TableField(fill = FieldFill.INSERT)
    private String createName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)

    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新人名称
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateName;

    /**
     * 更新人时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除标识：0否 1是
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 开始任务人id
     */
    private Long startBy;

    /**
     * 开始任务人姓名
     */
    private String startName;

    /**
     * 完成任务人id
     */
    private Long finishedBy;

    /**
     * 完成任务人姓名
     */
    private String finishedName;

    /**
     * 取消人
     */
    private Long cancelBy;

    /**
     * 取消人姓名
     */
    private String cancelName;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 送货单号
     */
    private String sendOrder;

    /**
     * 班次
     * 早班 0
     * 晚班 1
     */
    private Integer classesId;

    /**
     * 工位id
     */
    private Long stationId;

    /**
     * 处理把localDateTime转成String
     */
    @TableField(exist = false)
    private String date;

    /**
     * 工位
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private QtBaseIdNameDTO station;

    /**
     * 工位名称
     */
    @TableField(value = "station ->> '$.name'",
            insertStrategy = FieldStrategy.NEVER,
            updateStrategy = FieldStrategy.NEVER,
            select = true)
    private String stationName;

    /**
     * 班次时间
     */
    private LocalDateTime classTime;

    /**
     * 物料名称
     */
    @TableField(exist = false)
    private String materialName;

    /**
     * 原始任务单号
     */
    @TableField(exist = false)
    private String  originalTaskCode;
}