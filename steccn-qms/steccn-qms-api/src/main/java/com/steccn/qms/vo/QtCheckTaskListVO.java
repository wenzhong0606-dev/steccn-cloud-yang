package com.steccn.qms.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 巡检任务列表vo
 *
 * @author AndrewLuo
 * @date 2022/09/09
 */
@Data
public class QtCheckTaskListVO {
    /**
     * 任务id
     */
    private Long id;

    /**
     * 任务Code
     */
    private String code;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 到货时间
     */
    private LocalDateTime arrivalTime;

    /**
     * 供应商名
     */
    private String supplierName;

    /**
     * 物料使用车间名称
     */
    private String materialWorkshopName;


    /**
     * 生产订单
     */
    private String productOrder;

    /**
     * 生产车间
     */
    private String productWorkshopName;

    /**
     * 产线
     */
    private String productLineName;

    /**
     * 生产批次
     */
    private String productBatch;

    /**
     * 缺陷记录开始时间
     */
    private LocalDateTime defectStartTime;

    /**
     * 缺陷记录结束时间
     */
    private LocalDateTime defectEndTime;

    /**
     * 原始任务单号
     */
    private String originalTaskCode;

    /**
     * 发货单号
     */
    private String deliverOrder;

    /**
     * 发货时间
     */
    private LocalDateTime deliverTime;

    /**
     * 客户名字
     */
    private String customerName;

    /**
     * 发货数量
     */
    private Integer deliverNum;

    /**
     * 状态
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
     * 创建人
     */
    private String createName;

    /**
     * 任务创建时间
     */
    private LocalDateTime createTime;

    /**
     * 任务开始时间
     */
    private LocalDateTime startTime;

    /**
     * 任务完成时间
     */
    private LocalDateTime finishedTime;

}
