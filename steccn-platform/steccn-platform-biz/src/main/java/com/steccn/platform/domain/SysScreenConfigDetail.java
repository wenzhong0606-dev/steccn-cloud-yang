package com.steccn.platform.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 屏幕设置详情表
 * @TableName sys_screen_config_detail
 */
@TableName(value ="sys_screen_config_detail")
@Data
public class SysScreenConfigDetail implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 主表id
     */
    private Integer configId;

    /**
     * 值
     */
    private String price;

    /**
     * 类型
     */
    private String type;

    /**
     * 窗口宽度
     */
    private Integer width;

    /**
     * 窗口高度
     */
    private Integer height;

    /**
     * 左边距
     */
    private Integer marginLeft;

    /**
     * 上边距
     */
    private Integer marginTop;

    /**
     * 颜色
     */
    private String color;

    /**
     * 大小
     */
    private Integer size;

    /**
     * 字体
     */
    private String font;

    /**
     * 是否有边框 0 否 1是
     */
    private Integer isMargin;

    /**
     * 边框颜色
     */
    private String borderColor;

    /**
     * 是否删除 0 否 1是
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建人姓名
     */
    @TableField(fill = FieldFill.INSERT)
    private String createName;

    /**
     * 修改人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 修改人姓名
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}