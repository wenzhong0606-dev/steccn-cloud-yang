package com.steccn.oa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * oa流程其他参数DTO
 *
 * @author AndrewLuo
 * @date 2023/10/11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OaWorkflowOtherParamsDTO implements Serializable {
    /**
     * 用监控权限回收
     */
    private String ismonitor;
}
