package com.steccn.oa.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName formtable_main_441
 */
@TableName(value ="formtable_main_441")
@Data
public class FormtableMain441 implements Serializable {
    /**
     * 
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
    private String khmc;

    /**
     * 
     */
    private String glhth;

    /**
     * 
     */
    private Integer sqr;

    /**
     * 
     */
    private Integer sqbm;

    /**
     * 
     */
    private String sqrq;

    /**
     * 
     */
    private String fhyf;

    /**
     * 
     */
    private String jhdz;

    /**
     * 
     */
    private Integer jhlx;

    /**
     * 
     */
    private Integer jsbz;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}