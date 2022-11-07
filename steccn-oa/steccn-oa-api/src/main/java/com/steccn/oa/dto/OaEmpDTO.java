package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * OA查询分部下人员DTO
 *
 * @author AndrewLuo
 * @date 2022/11/07
 */
@Data
public class OaEmpDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分部id
     */
    private Integer subcompanyid;

    /**
     * 分页
     */
    private Integer current;

    /**
     * 名字
     */
    private String resourcename;
}
