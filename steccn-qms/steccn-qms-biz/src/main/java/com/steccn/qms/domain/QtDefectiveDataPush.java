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
 * @TableName qt_defective_data_push
 */
@TableName(value ="qt_defective_data_push")
@Data
public class QtDefectiveDataPush implements Serializable {
    /**
     * 主键id
     */
    @TableId
    private Long id;

    /**
     * 消息类型
     */
    private String msgType;

    /**
     * 扩展字段
     */
    private String extField;

    /**
     * 异常代码，正常时为空
     */
    private String errorCode;

    /**
     * 产品进入时间
     */
    private String productTime;

    /**
     * 线次
     */
    private String line;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}