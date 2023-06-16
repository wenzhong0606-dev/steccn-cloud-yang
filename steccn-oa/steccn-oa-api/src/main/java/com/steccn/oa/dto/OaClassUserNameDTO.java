package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * oa班组和
 *
 * @author AndrewLuo
 * @date 2023/06/15
 */
@Data
public class OaClassUserNameDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 班组名字
     */
    private String className;
    /**
     * 班组长名字
     */
    private String userName;
}
