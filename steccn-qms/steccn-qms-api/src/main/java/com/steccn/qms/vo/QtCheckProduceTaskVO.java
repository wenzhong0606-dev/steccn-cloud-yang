package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author wangzhixiong
 * 类说明
 * 生产管理模块下面的质量管理
 * 根据生产订单号查询化验抽检，首检，在线检，巡检的检验详情
 */
@Data
public class QtCheckProduceTaskVO implements Serializable {
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
     * 缺陷记录开始时间
     */
    private LocalDateTime defectStartTime;

    /**
     * 缺陷记录结束时间
     */
    private LocalDateTime defectEndTime;

}
