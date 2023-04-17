package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * OA部门详情DTO
 *
 * @author AndrewLuo
 * @date 2023/03/20
 */
@Data
public class OaOrgDetailDTO implements Serializable {
    /**
     * 部门编号
     */
    private String departmentcode;
    /**
     * 部门全称
     */
    private String departmentname;
    /**
     * 主键id
     */
    private String id;
    /**
     * 上级部门id
     */
    private String supdepid;
}
