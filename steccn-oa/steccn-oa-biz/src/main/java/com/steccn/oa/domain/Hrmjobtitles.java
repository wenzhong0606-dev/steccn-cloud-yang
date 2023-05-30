package com.steccn.oa.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName hrmjobtitles
 */
@TableName(value ="hrmjobtitles")
@Data
public class Hrmjobtitles implements Serializable {
    /**
     * 自增主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private String jobtitlemark;

    /**
     * 
     */
    private String jobtitlename;

    /**
     * 
     */
    private String jobtitleremark;

    /**
     * 
     */
    private Integer jobactivityid;

    /**
     * 
     */
    private Integer jobdepartmentid;

    /**
     * 
     */
    private String jobresponsibility;

    /**
     * 
     */
    private String jobcompetency;

    /**
     * 
     */
    private Integer jobdoc;

    /**
     * 
     */
    private String jobtitlecode;

    /**
     * 
     */
    private String outkey;

    /**
     * 
     */
    private String ecologyPinyinSearch;

    /**
     * 
     */
    private Date created;

    /**
     * 
     */
    private Integer creater;

    /**
     * 
     */
    private Date modified;

    /**
     * 
     */
    private Integer modifier;

    /**
     * 
     */
    private String uuid;

    /**
     * 
     */
    private String canceled;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}