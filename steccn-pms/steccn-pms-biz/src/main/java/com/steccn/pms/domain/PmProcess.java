package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.models.auth.In;
import lombok.Data;

/**
 * 生产工序表
 *
 * @TableName pm_process
 */
@TableName(value = "pm_process")
@Data
public class PmProcess implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 工序编码
     */
    private String code;

    /**
     * 工序类型
     */
    private Integer type;

    /**
     * 工序名称
     */
    private String name;

    /**
     * 工序状态
     */
    private Integer status;

    /**
     * 是否报工 0 否 1是
     */
    private Integer isReportingWorks;

    /**
     * 是否报检 0否 1是
     */
    private Integer isCustomsInspection;

    /**
     * 工艺指导书（文件id）
     */
    private Integer fileId;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 备注
     */
    private String node;

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