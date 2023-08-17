package com.steccn.oa.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName hrmdepartment
 */
@TableName(value ="hrmdepartment")
@Data
public class Hrmdepartment implements Serializable {
    /**
     * 自增主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private String departmentmark;

    /**
     * 
     */
    private String departmentname;

    /**
     * 
     */
    private Integer subcompanyid1;

    /**
     * 
     */
    private Integer supdepid;

    /**
     * 
     */
    private String allsupdepid;

    /**
     * 
     */
    private String canceled;

    /**
     * 
     */
    private String departmentcode;

    /**
     * 
     */
    private Integer coadjutant;

    /**
     * 
     */
    private String zzjgbmfzr;

    /**
     * 
     */
    private String zzjgbmfgld;

    /**
     * 
     */
    private String jzglbmfzr;

    /**
     * 
     */
    private String jzglbmfgld;

    /**
     * 
     */
    private String bmfzr;

    /**
     * 
     */
    private String bmfgld;

    /**
     * 
     */
    private String outkey;

    /**
     * 
     */
    private Integer budgetatuomoveorder;

    /**
     * 
     */
    private String ecologyPinyinSearch;

    /**
     * 
     */
    private Integer tlevel;

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
    private LocalDateTime modified;

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
    private BigDecimal showorder;

    /**
     * 
     */
    private Integer showorderoftree;

    /**
     * 
     */
    private String tenantKey;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}