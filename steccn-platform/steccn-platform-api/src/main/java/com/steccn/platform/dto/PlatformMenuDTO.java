package com.steccn.platform.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/08/05
 */
@Data
public class PlatformMenuDTO {
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
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    /**
     * 菜单编码
     */
    @NotBlank(message = "菜单编码不能为空")
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
}
