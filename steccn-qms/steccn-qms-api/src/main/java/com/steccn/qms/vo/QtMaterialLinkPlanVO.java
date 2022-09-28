package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 物料绑定的检验计划VO
 *
 * @author AndrewLuo
 * @date 2022/09/14
 */
@Data
public class QtMaterialLinkPlanVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 计划id
     */
    private Integer id;

    /**
     * 计划名字
     */
    private String name;
}
