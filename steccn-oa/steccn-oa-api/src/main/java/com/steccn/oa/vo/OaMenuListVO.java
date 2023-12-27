package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * oa菜单VO
 *
 * @author AndrewLuo
 * @date 2023/12/27
 */
@Data
public class OaMenuListVO implements Serializable {
    /**
     * 菜单名称
     */
    private String menuName;
    /**
     * 菜单的拼接url
     */
    private String url;
    /**
     * 菜单的id
     */
    private String id;
}
