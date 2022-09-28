package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName qt_defective_data_push_item
 */
@TableName(value ="qt_defective_data_push_item")
@Data
public class QtDefectiveDataPushItem implements Serializable {
    /**
     * 主键id
     */
    @TableId
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
    private String defectCount;

    /**
     * 主表id
     */
    private Long pushId;

    /**
     * 当前时间
     */
    private LocalDateTime createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}