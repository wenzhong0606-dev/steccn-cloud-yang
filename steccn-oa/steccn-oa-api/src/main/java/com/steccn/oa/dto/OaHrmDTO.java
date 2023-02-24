package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * oa获取人员列表DTO
 *
 * @author AndrewLuo
 * @date 2023/02/24
 */
@Data
public class OaHrmDTO implements Serializable {
    /**
     * 岗位
     */
    private String jobtitle;
}
