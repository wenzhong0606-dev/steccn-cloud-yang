package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 分组求和DTO
 *
 * @author AndrewLuo
 * @date 2023/06/06
 */
@Data
public class OaGroupCountDTO implements Serializable {
    private Integer count;
    private Integer groupId;
    private String groupName;
}
