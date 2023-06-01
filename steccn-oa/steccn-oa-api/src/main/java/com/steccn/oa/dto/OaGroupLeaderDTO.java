package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * oa班组长DTO
 *
 * @author AndrewLuo
 * @date 2023/06/01
 */
@Data
public class OaGroupLeaderDTO implements Serializable {
    private Integer userId;
    private String name;
    private Integer groupId;
}
