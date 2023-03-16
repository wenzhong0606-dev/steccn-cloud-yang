package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 不合格品处理表
 * @TableName qt_defect_product
 */
@TableName(value ="qt_defect_product")
@Data
public class QtDefectProduct implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 不合格品处理单号
     */
    private String code;

    /**
     * 标题
     */
    private String title;

    /**
     * 问题描述
     */
    private String desc;

    /**
     * 检验任务主键
     */
    private Long taskId;

    /**
     * 检验任务单号
     */
    private String taskCode;

    /**
     * 检验任务类型
     */
    private Long taskType;

    /**
     * 检验完成人员
     */
    private String taskFinishMan;

    /**
     * 检验完成时间
     */
    private LocalDateTime taskFinishTime;

    /**
     * 物料主键
     */
    private Long materialId;

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 生产订单号
     */
    private String produceOrderNo;

    /**
     * 生产批次号
     */
    private String produceBatchNo;

    /**
     * 供应商
     */
    private Integer supplier;

    /**
     * 样本数量
     */
    private BigDecimal sampleQuantity;

    /**
     * 合格数量
     */
    private BigDecimal goodQuantity;

    /**
     * 不合格数量
     */
    private BigDecimal badQuantity;

    /**
     * 处理人
     */
    private Long dealMan;

    /**
     * 0-待处理、1-待审批、2-已关闭
     */
    private Integer status;

    /**
     * 处理方式
     */
    private Integer dealWay;

    /**
     * 处理数量
     */
    private BigDecimal dealQuantity;

    /**
     * 备注
     */
    private String note;

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
}