package com.steccn.data.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @TableName qt_defect_collect_detail
 */
@TableName(value = "qt_defect_collect_detail")
@Data
public class QtDefectCollectDetail implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 缺陷种类
     */
    private String defectType;

    /**
     * 缺陷等级
     */
    private String level;

    /**
     * 缺陷数量
     */
    private Integer defectCount;

    /**
     * 主表id
     */
    private Long pushId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}