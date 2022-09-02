package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 巡检任务表
 * @TableName qt_check_task
 */
@TableName(value ="qt_check_task")
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
    private Integer materialId;

    /**
     * 车间
     */
    private Integer workshopId;

    /**
     * 产线
     */
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
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    private Long updateBy;

    /**
     * 更新人名称
     */
    private String updateName;

    /**
     * 更新人时间
     */
      private LocalDateTime updateTime;

    /**
     * 是否删除标识：0否 1是
     */
    @TableLogic
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}