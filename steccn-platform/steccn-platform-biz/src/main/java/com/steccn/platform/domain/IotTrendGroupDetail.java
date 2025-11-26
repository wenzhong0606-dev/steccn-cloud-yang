package com.steccn.platform.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

/**
 * 趋势组详情表
 * @TableName iot_trend_group_detail
 */
@TableName(value ="iot_trend_group_detail",autoResultMap = true)
@Data
public class IotTrendGroupDetail implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 趋势组id
     */
    private Integer trendId;

    /**
     * 名称
     */
    private String name;

    /**
     * 设备id
     */
    private String deviceId;

    /**
     * y轴名称
     */
    private String yName;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 测点id
     */
    private String pointId;

    /**
     * 聚合规则
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> aggregations;

    /**
     * 创建者
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 
     */
    @TableField(fill = FieldFill.INSERT)
    private String createName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateName;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 备注信息
     */
    private String remarks;

    /**
     * 是否删除标识：0否 1是
     */
    @TableField(fill = FieldFill.INSERT)
    @TableLogic
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}