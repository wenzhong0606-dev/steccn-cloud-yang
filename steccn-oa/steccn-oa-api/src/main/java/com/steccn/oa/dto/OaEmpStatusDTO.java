package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Oa在职人员状态DTO
 *
 * @author AndrewLuo
 * @date 2023/03/16
 */
@Data
public class OaEmpStatusDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 状态 8为在职
     */
    private String status;

    /**
     * 名字
     */
    private String resourcename;

    /**
     * 每页几条
     */
    private Integer pageSize;

    /**
     * 当前页
     */
    private Integer current;

    /**
     * 登录名
     */
    private String loginid;
}
