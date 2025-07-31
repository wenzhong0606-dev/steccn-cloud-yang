package com.steccn.platform.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/07/30
 */
@Data
public class PlatformOfficeDTO {
    private Integer id;

    /**
     * 父级id
     */
    private Integer parentId;

    /**
     * 菜单名称
     */
    @NotBlank(message = "机构名称不能为空")
    private String officeName;

    /**
     * 菜单编码
     */
    @NotBlank(message = "机构编码不能为空")
    private String officeCode;

    /**
     * 排序
     */
    private Integer sort;
}
