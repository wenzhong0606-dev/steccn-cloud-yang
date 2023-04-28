package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * oa考勤统计DTO
 *
 * @author AndrewLuo
 * @date 2023/04/18
 */
@Data
public class OaAttendanceDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 指定人员id
     */
    private String resourceId;
    /**
     * 2-按月、1-按年
     */
    private String type;
    /**
     * 年月或者月份
     */
    private String typevalue;
}
