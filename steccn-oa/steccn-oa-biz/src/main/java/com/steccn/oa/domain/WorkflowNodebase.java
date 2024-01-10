package com.steccn.oa.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName workflow_nodebase
 */
@TableName(value ="workflow_nodebase")
@Data
public class WorkflowNodebase implements Serializable {
    /**
     * 自增主键
     */
    private Integer id;

    /**
     * 
     */
    private String nodename;

    /**
     * 
     */
    private String isstart;

    /**
     * 
     */
    private String isreject;

    /**
     * 
     */
    private String isreopen;

    /**
     * 
     */
    private String isend;

    /**
     * 
     */
    private Integer drawxpos;

    /**
     * 
     */
    private Integer drawypos;

    /**
     * 
     */
    private Integer totalgroups;

    /**
     * 
     */
    private String nodeattribute;

    /**
     * 
     */
    private Integer passnum;

    /**
     * 
     */
    private String isfreenode;

    /**
     * 
     */
    private Integer floworder;

    /**
     * 
     */
    private String signtype;

    /**
     * 
     */
    private String operators1;

    /**
     * 
     */
    private Integer requestid;

    /**
     * 
     */
    private Integer startnodeid;

    /**
     * 
     */
    private String operators;

    /**
     * 
     */
    private String ecologyPinyinSearch;

    /**
     * 
     */
    private String drawstyle;

    /**
     * 
     */
    private String uuid;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}