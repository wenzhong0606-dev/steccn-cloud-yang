package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 
 * 落板记录DTO
 */
@Data
public class QtCheckTaskDestoryInfoDTO implements Serializable {


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
     * 落板明细
     */
    private String destoryDetail;

    /**
     * 落板原因
     */
    private String destoryReason;

    /**
     * 落板开始时间
     */
    private LocalDateTime destoryStartTime;

    /**
     * 落板结束时间
     */
    private LocalDateTime destoryEndTime;

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
    private Integer delFlag;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;

    private static final long serialVersionUID = 1L;
}