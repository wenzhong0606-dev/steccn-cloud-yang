package com.steccn.platform.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/08/05
 */
@Data
public class PlatformMenuVO {
    /**
     * 菜单id
     */
    private Integer id;

    /**
     * 父级id
     */
    private Integer parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单编码
     */
    private String menuCode;

    /**
     * 路由路径
     */
    private String routerPath;

    /**
     * 组件路径
     */
    private String componentPath;

    /**
     * 是否显示(0显示 1隐藏)
     */
    private Integer showFlag;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 菜单类型(0菜单 1权限)
     */
    private Integer menuType;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 菜单用途(0web 1pc)
     */
    private Integer menuUsage;

    /**
     * 是否绑定0未绑定 1已绑定
     */
    private Integer isBind;

    private List<PlatformMenuVO> children;
}
