package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/11/07
 */
@Data
public class OaEmpVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 人员id
     */
    private Integer id;

    /**
     * 姓名
     */
    private String lastName;

    /**
     * 职务
     */
    private String jobtitlespan;
}
