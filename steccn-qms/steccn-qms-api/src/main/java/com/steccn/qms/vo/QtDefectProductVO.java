package com.steccn.qms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class QtDefectProductVO {

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
    private String description;

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
     * 检验任务类型名称
     */
    private String taskTypeName;

    /**
     * 检验完成人员
     */
    private Long taskFinishMan;


    /**
     * 检验完成人员名称
     */
    private String taskFinishManName;

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
     * 物料名称
     */
    private String materialName;

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
     * 处理人名称
     */
    private String dealManName;

    /**
     * 0-待处理、1-待审批、2-已关闭
     */
    private Integer status;

    /**
     * 处理方式
     */
    private Integer dealWay;


    /**
     * 处理方式名称
     */
    private String dealWayName;

    /**
     * 处理数量
     */
    private BigDecimal dealQuantity;

    /**
     * 备注
     */
    private String note;
}
