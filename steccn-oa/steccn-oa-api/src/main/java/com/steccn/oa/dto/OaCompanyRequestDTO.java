package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/11/03
 */
@Data
public class OaCompanyRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String showCanceled;

    private String rightStr;

    private Integer type;

    private Integer id;

    private Integer isVirtual;

    private String keywords;

    private Integer virtualCompanyid;

    private Integer isLoadSubDepartment;
}
