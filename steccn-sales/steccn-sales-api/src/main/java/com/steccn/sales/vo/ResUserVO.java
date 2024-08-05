package com.steccn.sales.vo;

import lombok.Data;

/**
 * 用户信息VO
 *
 * @author AndrewLuo
 * @date 2024/08/05
 */
@Data
public class ResUserVO {
    /**
     * 姓名
     */
    private String name;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 物流公司Id
     */
    private Integer logisticsCompanyId;
}
