package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * oa班次班组用户VO
 *
 * @author AndrewLuo
 * @date 2023/07/26
 */
@Data
public class OaClassVO implements Serializable {
    /**
     * 用户id
     */
    private Integer userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 班次
     */
    private String classOrder;
    /**
     * 班组
     */
    private String classGroup;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
