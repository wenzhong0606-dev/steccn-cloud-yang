package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * oa人员信息
 *
 * @author AndrewLuo
 * @date 2022/10/14
 */
@Data
public class OaUserIdAndName implements Serializable {
    private Integer id;
    private String name;
    private String workcode;
}
