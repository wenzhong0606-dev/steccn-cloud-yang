package com.steccn.oa.dto;

import lombok.Data;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/09/02
 */
@Data
public class OaUserInfo {
    private String departmentName;
    private String name;
    private String headPortrait;
    private Boolean status;
}
