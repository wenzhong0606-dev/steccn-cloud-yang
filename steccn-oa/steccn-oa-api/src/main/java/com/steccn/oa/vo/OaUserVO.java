package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * oa用户Vo
 *
 * @author AndrewLuo
 * @date 2023/08/08
 */
@Data
public class OaUserVO implements Serializable {
    /**
     * oaId
     */
    private Integer id;
    /**
     * 登录名
     */
    private String userName;
    /**
     * oa姓名
     */
    private String name;
    /**
     * 部门id
     */
    private Integer departmentId;
    /**
     * 部门名称
     */
    private String departmentName;
    /**
     * 岗位id
     */
    private Integer jobTitleId;
    /**
     * 岗位名称
     */
    private String jobTitleName;
}
