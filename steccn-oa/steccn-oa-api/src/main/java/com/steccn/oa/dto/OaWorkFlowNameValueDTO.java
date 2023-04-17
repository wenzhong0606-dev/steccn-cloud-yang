package com.steccn.oa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * oa创建流程DTO
 *
 * @author AndrewLuo
 * @date 2023/01/16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OaWorkFlowNameValueDTO implements Serializable {
    private String fieldName;
    private String fieldValue;
}
