package com.steccn.oa.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName docdetaillog
 */
@TableName(value ="docdetaillog")
@Data
public class Docdetaillog implements Serializable {
    /**
     * 自增主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private Integer docid;

    /**
     * 
     */
    private String docsubject;

    /**
     * 
     */
    private Integer doccreater;

    /**
     * 
     */
    private String operatetype;

    /**
     * 
     */
    private String operatedesc;

    /**
     * 
     */
    private Integer operateuserid;

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
    private String clientaddress;

    /**
     * 
     */
    private String usertype;

    /**
     * 
     */
    private String creatertype;

    /**
     * 
     */
    private String operateitem;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}