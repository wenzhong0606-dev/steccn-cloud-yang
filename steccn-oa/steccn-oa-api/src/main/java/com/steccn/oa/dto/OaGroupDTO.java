package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Oa考勤组信息
 *
 * @author AndrewLuo
 * @date 2023/04/04
 */
@Data
public class OaGroupDTO implements Serializable {
    private Long userId;
    private Integer groupId;
    private String groupName;
}
