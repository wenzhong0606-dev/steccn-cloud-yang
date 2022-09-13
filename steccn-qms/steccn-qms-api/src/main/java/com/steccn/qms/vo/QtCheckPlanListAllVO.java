package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 所有检验方案listVO
 *
 * @author AndrewLuo
 * @date 2022/09/13
 */
@Data
public class QtCheckPlanListAllVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 检验id
     */
    private Integer id;

    /**
     * 检验名字
     */
    private String name;

}
