package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * 考勤组范围
 * @author AndrewLuo
 * @date 2023/07/13
 */
@Data
public class OaKqGroupTimeRangeVO implements Serializable {
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer isAcross;
}
