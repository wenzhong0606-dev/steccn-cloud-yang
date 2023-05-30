package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * oa部门VO
 *
 * @author AndrewLuo
 * @date 2023/05/30
 */
@Data
public class OaDepartmentVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 数据id
     */
    private Integer id;

    /**
     * 父级id
     */
    private Integer superId;

    /**
     * 部门名字
     */
    private String departmentName;

    /**
     * 排序
     */
    private Integer tLevel;
}
