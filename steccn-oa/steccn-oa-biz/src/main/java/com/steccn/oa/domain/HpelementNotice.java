package com.steccn.oa.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

/**
 * 
 * @TableName hpelement_notice
 */
@TableName(value ="hpelement_notice")
@Data
public class HpelementNotice implements Serializable {
    /**
     * 自增主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private String title;

    /**
     * 
     */
    private String content;

    /**
     * 
     */
    private String imgsrc;

    /**
     * 
     */
    private Integer creator;

    /**
     * 
     */
    private String creatortype;

    /**
     * 
     */
    private LocalDate createdate;

    /**
     * 
     */
    private LocalTime createtime;

    /**
     * 
     */
    private LocalDate lastupdatedate;

    /**
     * 
     */
    private LocalTime lastupdatetime;

    /**
     * 
     */
    private Integer eid;

    /**
     * 
     */
    private Integer relatedocid;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}