package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * oa排班信息VO
 *
 * @author AndrewLuo
 * @date 2023/03/02
 */
@Data
public class OaRosterRecordVO implements Serializable {
    /**
     * 员工工号
     */
    private String workcode;

    /**
     * 班次
     */
    private String serial;

    /**
     * 部门全称
     */
    private String departmentname;

    /**
     * 分部名称
     */
    private String subname;

    /**
     * 考勤日期
     */
    private String kqdate;

    /**
     * 员工姓名
     */
    private String lastname;

    /**
     * 用户id
     */
    private Integer userId;
}
