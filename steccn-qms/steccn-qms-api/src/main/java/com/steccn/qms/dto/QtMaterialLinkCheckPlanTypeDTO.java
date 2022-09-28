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
    private String checkTypeId;
    /**
     * 检验类型
     */
    private String checkType;
    /**
     * 检验code
     */
    private Integer checkTypeCode;
    /**
     *检验计划id
     */
    private Integer checkPlanId;
    /**
     * 检验计划code
     */
    private String checkPlanCode;
    /**
     * 检验计划名称
     */
    private String checkPlanName;

    private static final long serialVersionUID = 1L;
}
