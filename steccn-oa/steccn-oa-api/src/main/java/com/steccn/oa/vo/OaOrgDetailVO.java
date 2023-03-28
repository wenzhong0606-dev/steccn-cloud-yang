package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * oa部门详情VO
 *
 * @author AndrewLuo
 * @date 2023/03/21
 */
@Data
public class OaOrgDetailVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 部门id
     */
    private Integer id;
    /**
     * 部门名字
     */
    private String departmentname;
    /**
     * 部门code
     */
    private String departmentcode;

    /**
     * 分部id
     */
    private String subcompanyid1;
}
