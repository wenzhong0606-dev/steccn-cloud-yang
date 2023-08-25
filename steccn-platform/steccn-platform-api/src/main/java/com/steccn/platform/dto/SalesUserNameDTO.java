package com.steccn.platform.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 销售系统传参用户名
 *
 * @author AndrewLuo
 * @date 2023/08/23
 */
@Data
public class SalesUserNameDTO implements Serializable {
    private String login;
}
