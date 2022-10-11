package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 出站信息
 * @TableName pms_process_out_station
 */
@TableName(value ="pms_process_out_station")
@Data
public class PmsProcessOutStation implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 出站时间
     */
    private LocalDateTime outStationTime;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单位id
     */
    private Long unitId;

    /**
     * 批次号
     */
    private Long batchNo;

    /**
     * 工位id
     */
    private Integer stationId;

    /**
     * 工序id
     */
    private Long processId;

    /**
     * 创建人id
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建人名称
     */
    @TableField(fill = FieldFill.INSERT)
    private String createName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新人名称
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateName;

    /**
     * 更新人时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除标识：0否 1是
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}