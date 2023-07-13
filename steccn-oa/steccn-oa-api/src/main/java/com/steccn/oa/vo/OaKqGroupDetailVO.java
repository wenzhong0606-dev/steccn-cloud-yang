package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * oa排班规则VO
 *
 * @author AndrewLuo
 * @date 2023/07/05
 */
@Data
public class OaKqGroupDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 考勤组Id
     */
    private Integer groupId;
    /**
     * 考勤组名字
     */
    private String groupName;
    /**
     * 考勤类型 1.固定班制 2.排班制 3.弹性工作制
     */
    private Integer kqType;

    /**
     * 开始日期
     */
    private LocalDate startTime;

    /**
     * 弹性工作打卡时间
     */
    private List<String> flexible;

    /**
     * 周期
     */
    private OaKqGroupShiftCycleVO shiftCycle;
}
