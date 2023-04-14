package com.steccn.oa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 操作人
 *
 * @author AndrewLuo
 * @date 2023/03/28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OaOperatorParamDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 流程id
     */
    private Integer requestId;

    /**
     * 节点id
     */
    private Integer nodeId;
}
