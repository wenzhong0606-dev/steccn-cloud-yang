package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 转换oa用户类型dto
 *
 * @author AndrewLuo
 * @date 2023/03/02
 */
@Data
public class OaUserInfoTmpDTO implements Serializable {
    private String departmentname;
    private String lastname;
    private String icon;
    private Integer id;
}
