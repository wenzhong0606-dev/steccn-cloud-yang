package com.steccn.qms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.steccn.common.valid.InsertGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 新增巡检任务DTO
 *
 * @author AndrewLuo
 * @date 2022/09/09
 */
@Data
public class QtAddCheckTaskDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer type;

    /**
     * 物料id
     */
    @NotNull(groups = {InsertGroup.class}, message = "物料id不能为空")
    private Long materialId;

    /**
     * 物料车间
     */
    private QtBaseIdNameDTO materialWorkshop;

    /**
     * 生产车间
     */
    private QtBaseIdNameDTO productWorkshop;

    /**
     * 产线
     */
    private QtBaseIdNameDTO productLine;

    /**
     * 生产批次
     */
    private String productBatch;

    /**
     * 生产工单
     */
    private String productOrder;

    /**
     * 检验方案id
     */
    @NotNull(groups = {InsertGroup.class}, message = "检验方案不能为空")
    private Integer checkPlanId;

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
    private QtBaseIdNameDTO supplier;

    /**
     * 客户id
     */
    private QtBaseIdNameDTO customer;

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
    @NotNull(groups = {InsertGroup.class}, message = "任务类型不能为空")
    private Integer taskType;

    /**
     * 发货单号
     */
    private String deliverOrder;

    /**
     * 发货数量
     */
    private Integer deliverNum;

    /**
     * 送货单号
     */
    private String sendOrder;

    /**
     * 原始任务id
     */
    private Long originalId;
}
