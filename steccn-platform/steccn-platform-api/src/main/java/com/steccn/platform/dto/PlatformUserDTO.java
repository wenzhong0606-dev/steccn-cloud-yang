package com.steccn.platform.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户DTO
 *
 * @author AndrewLuo
 * @date 2025/07/29
 */
@Data
public class PlatformUserDTO extends BasePageDTO {
    /**
     * id
     */
    private Long id;

    /**
     * 用户编码
     */
    private String userCode;

    /**
     * 登录账号
     */
    @NotBlank(message = "登录名不能为空")
    private String userName;

    /**
     * 用户昵称
     */
    @NotBlank(message = "用户名称不能为空")
    private String name;

    /**
     * 登录密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String mobile;

    /**
     * 办公电话
     */
    private String phone;

    /**
     * 用户性别（0男 1女）
     */
    private Integer sex;

    /**
     * 头像路径
     */
    private String avatar;

    /**
     * 状态（0正常 1停用）
     */
    private Integer status;

    /**
     * 机构id
     */
    private Integer officeId;

    /**
     * 岗位id
     */
    private Integer postId;

    /**
     * 设备数量
     */
    private Integer deviceNum;

    /**
     * 备注
     */
    private String remarks;
}
