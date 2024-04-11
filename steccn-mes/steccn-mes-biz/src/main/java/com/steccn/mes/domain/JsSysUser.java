package com.steccn.mes.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 用户表
 * @TableName js_sys_user
 */
@TableName(value ="js_sys_user")
@Data
public class JsSysUser implements Serializable {
    /**
     * 用户编码
     */
    private String userCode;

    /**
     * 登录账号
     */
    private String loginCode;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 登录密码
     */
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
     * 用户性别
     */
    private String sex;

    /**
     * 头像路径
     */
    private String avatar;

    /**
     * 个性签名
     */
    private String sign;

    /**
     * 绑定的微信号
     */
    private String wxOpenid;

    /**
     * 绑定的手机串号
     */
    private String mobileImei;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 用户类型引用编号
     */
    private String refCode;

    /**
     * 用户类型引用姓名
     */
    private String refName;

    /**
     * 管理员类型（0非管理员 1系统管理员  2二级管理员）
     */
    private String mgrType;

    /**
     * 密码安全级别（0初始 1很弱 2弱 3安全 4很安全）
     */
    private Integer pwdSecurityLevel;

    /**
     * 密码最后更新时间
     */
    private Date pwdUpdateDate;

    /**
     * 密码修改记录
     */
    private String pwdUpdateRecord;

    /**
     * 密保问题
     */
    private String pwdQuestion;

    /**
     * 密保问题答案
     */
    private String pwdQuestionAnswer;

    /**
     * 密保问题2
     */
    private String pwdQuestion2;

    /**
     * 密保问题答案2
     */
    private String pwdQuestionAnswer2;

    /**
     * 密保问题3
     */
    private String pwdQuestion3;

    /**
     * 密保问题答案3
     */
    private String pwdQuestionAnswer3;

    /**
     * 密码问题修改时间
     */
    private Date pwdQuestUpdateDate;

    /**
     * 最后登陆IP
     */
    private String lastLoginIp;

    /**
     * 最后登陆时间
     */
    private Date lastLoginDate;

    /**
     * 冻结时间
     */
    private Date freezeDate;

    /**
     * 冻结原因
     */
    private String freezeCause;

    /**
     * 用户权重（降序）
     */
    private Integer userWeight;

    /**
     * 状态（0正常 1删除 2停用 3冻结）
     */
    private String status;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateDate;

    /**
     * 备注信息
     */
    private String remarks;

    /**
     * 租户代码
     */
    private String corpCode;

    /**
     * 租户名称
     */
    private String corpName;

    /**
     * 扩展 String 1  
     */
    private String extendS1;

    /**
     * 扩展 String 2
     */
    private String extendS2;

    /**
     * 扩展 String 3
     */
    private String extendS3;

    /**
     * 扩展 String 4
     */
    private String extendS4;

    /**
     * 扩展 String 5
     */
    private String extendS5;

    /**
     * 扩展 String 6
     */
    private String extendS6;

    /**
     * 扩展 String 7
     */
    private String extendS7;

    /**
     * 扩展 String 8
     */
    private String extendS8;

    /**
     * 扩展 Integer 1
     */
    private Long extendI1;

    /**
     * 扩展 Integer 2
     */
    private Long extendI2;

    /**
     * 扩展 Integer 3
     */
    private Long extendI3;

    /**
     * 扩展 Integer 4
     */
    private Long extendI4;

    /**
     * 扩展 Float 1
     */
    private BigDecimal extendF1;

    /**
     * 扩展 Float 2
     */
    private BigDecimal extendF2;

    /**
     * 扩展 Float 3
     */
    private BigDecimal extendF3;

    /**
     * 扩展 Float 4
     */
    private BigDecimal extendF4;

    /**
     * 扩展 Date 1
     */
    private Date extendD1;

    /**
     * 扩展 Date 2
     */
    private Date extendD2;

    /**
     * 扩展 Date 3
     */
    private Date extendD3;

    /**
     * 扩展 Date 4
     */
    private Date extendD4;

    /**
     * CUCC 主键
     */
    private String userId;

    /**
     * CUCC 关联公司表id
     */
    private String subcompanyId;

    /**
     * CUCC 关联部门表
     */
    private String orgId;

    /**
     * CUCC 生日
     */
    private Date birthday;

    /**
     * CUCC枣庄公司id
     */
    private String plantId;

    /**
     * 扩展 JSON
     */
    private String extendJson;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}