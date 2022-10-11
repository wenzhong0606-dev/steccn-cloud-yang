package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName qt_check_task_copy
 */
@TableName(value ="qt_check_task_copy")
@Data
public class QtCheckTaskCopy implements Serializable {
    private Integer id;

    private Long taskId;

    private Integer copy;

    private String sampleNum;

    private String licensePlate;

    private Integer analyst;

    private String analystName;

    private Date samplingTime;

    private Date analystTime;

    private Integer ingredientNum;

    private Long createBy;

    private String createName;

    private Long updateBy;

    private String updateName;

    private Integer delFlag;

    private static final long serialVersionUID = 1L;
}