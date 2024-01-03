package com.steccn.mes.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 员工表
 * @TableName js_sys_employee
 */
@TableName(value ="js_sys_employee")
@Data
public class JsSysEmployee implements Serializable {
    /**
     * 员工编码
     */
    private String empCode;

    /**
     * 员工姓名
     */
    private String empName;

    /**
     * 员工英文名
     */
    private String empNameEn;

    /**
     * 员工工号
     */
    private String empNo;

    /**
     * 机构编码
     */
    private String officeCode;

    /**
     * 机构名称
     */
    private String officeName;

    /**
     * 公司编码
     */
    private String companyCode;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 状态（0在职 1删除 2离职）
     */
    private String status;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createDate;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateDate;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}