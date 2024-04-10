package com.steccn.pms.dto;

import lombok.Data;

import java.util.List;

/**
 * 劳保用品领用清单DTO
 *
 * @author AndrewLuo
 * @date 2024/04/08
 */
@Data
public class PmsStandardListDTO {
    /**
     * 部门Id
     */
    private List<String> officeId;
    /**
     * 岗位Id
     */
    private List<String> postId;
    /**
     * 用户名称
     */
    private String userName;
    /**
     * 物料Id
     */
    private List<Long> materialId;
}
