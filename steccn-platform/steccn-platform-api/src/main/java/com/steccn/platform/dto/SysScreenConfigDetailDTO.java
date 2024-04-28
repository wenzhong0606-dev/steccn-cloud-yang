package com.steccn.platform.dto;

import lombok.Data;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2024/04/22
 */
@Data
public class SysScreenConfigDetailDTO {
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
}
