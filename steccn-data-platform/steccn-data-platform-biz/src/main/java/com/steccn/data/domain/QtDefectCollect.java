package com.steccn.data.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 
 * @author yequan
 * @TableName qt_defect_collect
 */
@TableName(value ="qt_defect_collect")
@Data
public class QtDefectCollect implements Serializable {
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
    private LocalDateTime productTime;

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