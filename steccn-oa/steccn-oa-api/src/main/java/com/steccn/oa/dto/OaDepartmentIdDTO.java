package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Oa部门idDTO
 *
 * @author AndrewLuo
 * @date 2023/03/20
 */
@Data
public class OaDepartmentIdDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 部门id
     */
    private String departmentId;
}
