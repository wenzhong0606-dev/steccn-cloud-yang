package com.steccn.oa.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName workflow_requestlog
 */
@TableName(value ="workflow_requestlog")
@Data
public class WorkflowRequestlog implements Serializable {
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
    private String logtype;

    /**
     * 
     */
    private String operatedate;

    /**
     * 
     */
    private String operatetime;

    /**
     * 
     */
    private Integer operator;

    /**
     * 
     */
    private String remark1;

    /**
     * 
     */
    private String clientip;

    /**
     * 
     */
    private Integer operatortype;

    /**
     * 
     */
    private Integer destnodeid;

    /**
     * 
     */
    private String receivedpersons1;

    /**
     * 
     */
    private Integer showorder;

    /**
     * 
     */
    private Integer agentorbyagentid;

    /**
     * 
     */
    private String agenttype;

    /**
     * 自增主键
     */
    private Integer logid;

    /**
     * 
     */
    private String remark;

    /**
     * 
     */
    private String annexdocids;

    /**
     * 
     */
    private Integer requestlogid;

    /**
     * 
     */
    private Integer operatordept;

    /**
     * 
     */
    private String signdocids;

    /**
     * 
     */
    private String signworkflowids;

    /**
     * 
     */
    private String receivedpersons;

    /**
     * 
     */
    private String ismobile;

    /**
     * 
     */
    private String handwrittensign;

    /**
     * 
     */
    private Integer speechattachment;

    /**
     * 
     */
    private String receivedpersonids;

    /**
     * 
     */
    private String remarklocation;

    /**
     * 
     */
    private String issubmitdirect;

    /**
     * 
     */
    private String remarkquote;

    /**
     * 
     */
    private String fulltextannotation;

    /**
     * 
     */
    private String speechattachmente9;

    /**
     * 
     */
    private String uuid;

    /**
     * 
     */
    private Integer operatorsub;

    /**
     * 
     */
    private Integer operatorjob;

    /**
     * 
     */
    private String isrobotnode;

    /**
     * 
     */
    private String seclevel;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}