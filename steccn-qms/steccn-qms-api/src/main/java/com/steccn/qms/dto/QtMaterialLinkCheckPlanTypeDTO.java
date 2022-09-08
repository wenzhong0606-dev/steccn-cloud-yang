package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/09/08
 */
@Data
public class QtMaterialLinkCheckPlanTypeDTO implements Serializable {
    /**
     * 检验类型id
     */
    private String checkType;
    /**
     *检验计划id
     */
    private Integer checkPlanId;
}
