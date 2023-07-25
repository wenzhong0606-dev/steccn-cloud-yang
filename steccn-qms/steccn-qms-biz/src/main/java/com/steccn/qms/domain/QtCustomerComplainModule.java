package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 客诉记录组件明细表

 * @TableName qt_customer_complain_module
 */
@TableName(value ="qt_customer_complain_module")
@Data
public class QtCustomerComplainModule implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 客诉记录id
     */
    private Long customerComplainId;

    /**
     * 组件序列号
     */
    private String moduleNumber;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    private Long updateBy;

    /**
     * 更新人名称
     */
    private String updateName;

    /**
     * 更新人时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除标识：0否 1是
     */
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}