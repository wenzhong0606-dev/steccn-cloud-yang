package com.steccn.mes.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息VO
 *
 * @author AndrewLuo
 * @date 2024/04/11
 */
@Data
public class MesUserInfoVO implements Serializable {
    private String loginCode;
    private String name;
    private String depCode;
    private String depName;
    private String postCode;
    private String poseName;
}
