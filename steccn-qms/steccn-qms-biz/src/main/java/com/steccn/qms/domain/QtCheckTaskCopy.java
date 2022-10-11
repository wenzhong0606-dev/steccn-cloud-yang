package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName qt_check_task_copy
 */
@TableName(value ="qt_check_task_copy")
@Data
public class QtCheckTaskCopy implements Serializable {
    /**
     * 主键id
     */
    @TableId
    private Integer id;

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 样本数
     */
    private Integer copy;

    /**
     * 样本编号
     */
    private String sampleNum;

    /**
     * 车牌号
     */
    private String licensePlate;

    /**
     * 分析人id
     */
    private Integer analyst;

    /**
     * 分析人姓名
     */
    private String analystName;

    /**
     * 取样时间
     */
    private Date samplingTime;

    /**
     * 分析日期
     */
    private Date analystTime;

    /**
     * 配料附数
     */
    private Integer ingredientNum;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人姓名
     */
    private String createName;

    /**
     * 修改人id
     */
    private Long updateBy;

    /**
     * 修改人姓名
     */
    private String updateName;

    /**
     * 是否删除标识：0否 1是
     */
    @TableLogic
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}