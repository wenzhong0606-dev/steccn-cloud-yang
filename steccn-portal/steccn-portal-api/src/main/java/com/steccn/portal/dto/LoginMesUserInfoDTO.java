package com.steccn.portal.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * @author yequan
 * @desc mess登录用户信息
 * @date 2023/9/12
 */
@Data
@ApiModel("mess登录用户信息")
public class LoginMesUserInfoDTO implements Serializable {

    private String userCode;

    private String loginCode;

    private String officeCode;

    private String username;

    private String officeType;
}
