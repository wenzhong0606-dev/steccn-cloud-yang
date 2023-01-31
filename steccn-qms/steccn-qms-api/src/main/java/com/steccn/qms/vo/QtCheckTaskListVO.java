package com.steccn.qms.vo;

import com.steccn.qms.dto.QtBaseIdNameDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 巡检任务列表vo
 *
 * @author AndrewLuo
 * @date 2022/09/09
 */
@Data
public class QtCheckTaskListVO implements Serializable {
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
     * 物料id
     */
    private Long materialId;

    /**
     * 到货时间
     */
    private LocalDate arrivalTime;

    /**
     * 供应商名
     */
    private QtBaseIdNameDTO supplier;

    /**
     * 物料使用车间名称
     */
    private QtBaseIdNameDTO materialWorkshop;

    /**
     * 任务类型
     */
    private Integer taskType;

    /**
     * 生产订单
     */
    private String productOrder;

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
    private LocalDate deliverTime;

    /**
     * 客户名字
     */
    private QtBaseIdNameDTO customer;

    private String productWorkshopName;

    private String productLineName;

    private String materialWorkshopName;

    private String supplierName;

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

    /**
     * 创建人姓名
     */
    private String startName;

    /**
     * 缺陷总数
     */
    private Integer defectNum;

    /**
     * 计划id
     */
    private Integer checkPlanId;

    /**
     * 计划name
     */
    private String checkPlanName;

    /**
     * 完成任务人id
     */
    private Long finishedBy;

    /**
     * 完成任务人姓名
     */
    private String finishedName;

    /**
     * 送货单号
     */
    private String sendOrder;

    /**
     * 工位id
     */
    private Integer stationId;

    /**
     * 工位名称
     */
    private String stationName;

    /**
     * 工位
     */
    private QtBaseIdNameDTO station;

    /**
     * 班次
     */
    private String classes;

    /**
     * 生产产线id
     */
    private String productLineId;

    /**
     * 生产车间id
     */
    private String productWorkshopId;

    /**
     * 生产产线code
     */
    private String productLineCode;

    /**
     * 生产车间code
     */
    private String productWorkshopCode;

    /**
     * 抽检时间
     */
    private LocalDateTime spotTime;

    /**
     * 班次id
     */
    private Integer classesId;

}
