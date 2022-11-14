package com.steccn.qms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/09/09
 */
@Data
public class QtCheckTaskListDTO extends BasePageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 检验类型Code
     * 来料检1/首检2/在线检3/巡检4/出货检5/化验抽检6/配料抽检7
     */
    private Integer checkTypeCode;

    /**
     * 任务状态 0待检验 1检验中 2已检验 3已取消 4暂存
     */
    private Integer status;

    /**
     * 状态可传多个
     */
    private List<Integer> statusList;

    /**
     * 物料名称或者编码
     */
    private String materialNameOrCode;

    /**
     * 供应商id
     */
    private Integer supplierId;

    /**
     * 物料车间id
     */
    private Integer materialWorkshopId;

    /**
     * 生产车间id
     */
    private Integer productWorkshopId;

    /**
     * 检验结果
     */
    private String result;

    /**
     * 生产单号
     */
    private String productOrder;

    /**
     * 产线id
     */
    private Integer productLineId;

    /**
     * 生产批次
     */
    private String productBatch;

    /**
     * 原始任务单号
     */
    private String originalTaskCode;

    /**
     * 发货单号
     */
    private String deliverOrder;

    /**
     * 发货日期开始
     */
    private LocalDateTime deliverTimeStart;

    /**
     * 发货日期结束
     */
    private LocalDateTime deliverTimeEnd;

    /**
     * 到货日期开始
     */
    private LocalDate arrivalTimeStart;

    /**
     * 到货日期结束
     */
    private LocalDate arrivalTimeEnd;

    /**
     * 客户id
     */
    private Integer customerId;

    /**
     * 创建日期开始
     */
    private LocalDateTime createTimeStart;

    /**
     * 创建日期结束
     */
    private LocalDateTime createTimeEnd;

    /**
     * 创建人Id
     */
    private Integer createBy;

    /**
     * 工位id
     */
    private Integer stationId;

    /**
     * 工位名称
     */
    private String stationName;

    /**
     * 工位JSON
     */
    private QtBaseIdNameDTO station;

    /**
     * 班次
     */
    private String classes;

}
