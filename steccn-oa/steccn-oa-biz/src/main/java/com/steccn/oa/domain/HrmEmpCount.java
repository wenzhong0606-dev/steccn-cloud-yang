package com.steccn.oa.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName hrm_emp_count
 */
@TableName(value ="hrm_emp_count")
@Data
public class HrmEmpCount implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 月份
     */
    private LocalDate month;

    /**
     * 在职人数
     */
    private Integer onJob;

    /**
     * 离职人数
     */
    private Integer leaveJob;

    /**
     * 是否删除 0 否 1是
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}