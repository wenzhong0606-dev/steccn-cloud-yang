package com.steccn.platform.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/07/30
 */
@Data
public class PlatformOfficeVO {
    private Integer id;

    /**
     * 父级id
     */
    private Integer parentId;

    /**
     * 菜单名称
     */
    private String officeName;

    /**
     * 菜单编码
     */
    private String officeCode;

    /**
     * 排序
     */
    private Integer sort;

    List<PlatformOfficeVO> children;
}
