package com.steccn.platform.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.awt.*;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/07/29
 */
@Data
public class PlatformUserVO {
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
    private String userName;

    /**
     *
     */
    private String password;

    /**
     * 用户昵称
     */
    private String name;

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
     * 权限
     */
    private List<String> userScope;

    /**
     * 状态（0正常 1删除 2停用 3冻结）
     */
    private Integer status;
}
