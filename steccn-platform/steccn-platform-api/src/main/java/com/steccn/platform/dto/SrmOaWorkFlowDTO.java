package com.steccn.platform.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * oa流程信息推送DTO
 *
 * @author AndrewLuo
 * @date 2023/08/29
 */
@Data
public class SrmOaWorkFlowDTO implements Serializable {
    private String payload;
}
