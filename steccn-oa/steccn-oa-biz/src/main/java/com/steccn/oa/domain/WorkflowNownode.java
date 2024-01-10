package com.steccn.oa.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName workflow_nownode
 */
@TableName(value ="workflow_nownode")
@Data
public class WorkflowNownode implements Serializable {
    /**
     * 
     */
    private Integer requestid;

    /**
     * 
     */
    private Integer nownodeid;

    /**
     * 
     */
    private Integer nownodetype;

    /**
     * 
     */
    private Integer nownodeattribute;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}