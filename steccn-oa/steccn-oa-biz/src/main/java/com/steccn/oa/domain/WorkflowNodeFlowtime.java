package com.steccn.oa.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName workflow_node_flowtime
 */
@TableName(value ="workflow_node_flowtime")
@Data
public class WorkflowNodeFlowtime implements Serializable {
    /**
     * 
     */
    private Integer requestid;

    /**
     * 
     */
    private Integer workflowid;

    /**
     * 
     */
    private Integer nodeid;

    /**
     * 
     */
    private Integer nodeoperator;

    /**
     * 
     */
    private String receivedate;

    /**
     * 
     */
    private Long flowtime;

    /**
     * 
     */
    private String status;

    /**
     * 
     */
    private String isovertime;

    /**
     * 
     */
    private Integer departmentid;

    /**
     * 
     */
    private Integer subcompanyid1;

    /**
     * 
     */
    private Integer userstatus;

    /**
     * 
     */
    private String createdate;

    /**
     * 
     */
    private Integer workflowtype;

    /**
     * 
     */
    private String receivetime;

    /**
     * 
     */
    private Long overflowtime;

    /**
     * 
     */
    private Integer islasttimes;

    /**
     * 
     */
    private Integer viewtype;

    /**
     * 
     */
    private Integer usertype;

    /**
     * 
     */
    private Integer groupid;

    /**
     * 
     */
    private String createtime;

    /**
     * 
     */
    private Integer isremark;

    /**
     * 
     */
    private String seclevel;

    /**
     * 
     */
    private String overtime;

    /**
     * 
     */
    private String viewdate;

    /**
     * 
     */
    private String viewtime;

    /**
     * 
     */
    private Integer preisremark;

    /**
     * 
     */
    private Integer takisremark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}