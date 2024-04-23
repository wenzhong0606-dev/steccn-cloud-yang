package com.steccn.platform.dto;

import lombok.Data;

import java.util.List;

/**
 *  屏幕配置DTO
 *
 * @author AndrewLuo
 * @date 2024/04/22
 */
@Data
public class SysScreenConfigDTO {
    /**
     * id主键
     */
    private Integer id;
    /**
     * 标题
     */
    private String title;

    /**
     * 窗口宽度
     */
    private Integer width;

    /**
     * 窗口高度
     */
    private Integer height;

    /**
     * 排序
     */
    private Integer priority;

    /**
     * 是否有效 0 否 1是
     */
    private Integer isEffective;

    /**
     * 显示时长
     */
    private Integer displayDuration;

    /**
     * 详情
     */
    private List<SysScreenConfigDetailDTO> list;
}
