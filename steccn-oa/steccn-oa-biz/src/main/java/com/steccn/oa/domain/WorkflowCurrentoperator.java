package com.steccn.oa.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName workflow_currentoperator
 */
@TableName(value ="workflow_currentoperator")
@Data
public class WorkflowCurrentoperator implements Serializable {
    /**
     * 自增主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private Integer requestid;

    /**
     * 
     */
    private Integer userid;

    /**
     * 
     */
    private Integer groupid;

    /**
     * 
     */
    private Integer workflowid;

    /**
     * 
     */
    private Integer workflowtype;

    /**
     * 
     */
    private Integer usertype;

    /**
     * 
     */
    private Integer nodeid;

    /**
     * 
     */
    private Integer agentorbyagentid;

    /**
     * 
     */
    private String agenttype;

    /**
     * 
     */
    private Integer showorder;

    /**
     * 
     */
    private String receivedate;

    /**
     * 
     */
    private String receivetime;

    /**
     * 
     */
    private Integer viewtype;

    /**
     * 
     */
    private Integer iscomplete;

    /**
     * 
     */
    private Integer islasttimes;

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
    private Integer groupdetailid;

    /**
     * 
     */
    private String isreminded;

    /**
     * 
     */
    private String isprocessed;

    /**
     * 
     */
    private String wfreminduser;

    /**
     * 
     */
    private String wfusertypes;

    /**
     * 
     */
    private String isreject;

    /**
     * 
     */
    private String needwfback;

    /**
     * 
     */
    private String lastisremark;

    /**
     * 
     */
    private String isremindedCsh;

    /**
     * 
     */
    private String wfreminduserCsh;

    /**
     * 
     */
    private String wfusertypesCsh;

    /**
     * 
     */
    private Integer handleforwardid;

    /**
     * 
     */
    private Integer takisremark;

    /**
     * 
     */
    private String lastreminddatetime;

    /**
     * 
     */
    private String isprocessing;

    /**
     * 
     */
    private String opdatetime;

    /**
     * 
     */
    private Integer processuser;

    /**
     * 
     */
    private String isbereject;

    /**
     * 
     */
    private Integer isremark;

    /**
     * 
     */
    private Integer preisremark;

    /**
     * 
     */
    private String overtime;

    /**
     * 
     */
    private String overworktime;

    /**
     * 
     */
    private Integer takid;

    /**
     * 
     */
    private Integer multitaklevel;

    /**
     * 
     */
    private String istakout;

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
    private Integer isinmultitak;

    /**
     * 
     */
    private String firstviewdate;

    /**
     * 
     */
    private String firstviewtime;

    /**
     * 
     */
    private Integer operatetype;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}