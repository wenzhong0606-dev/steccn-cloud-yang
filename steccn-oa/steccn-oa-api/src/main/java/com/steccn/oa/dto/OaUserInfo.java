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
    /**
     * 名字
     */
    private String name;
    /**
     * 头像
     */
    private String headPortrait;
    /**
     * 状态
     */
    private Boolean status;
    /**
     * id
     */
    private String id;
}
