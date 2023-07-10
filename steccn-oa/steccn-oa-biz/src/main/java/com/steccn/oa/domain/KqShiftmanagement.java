package com.steccn.oa.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName kq_shiftmanagement
 */
@TableName(value ="kq_shiftmanagement")
@Data
public class KqShiftmanagement implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private String serial;

    /**
     * 
     */
    private String isresttimeopen;

    /**
     * 
     */
    private Integer shiftonoffworkcount;

    /**
     * 
     */
    private Integer shiftonoffworksectionsid;

    /**
     * 
     */
    private String punchsettings;

    /**
     * 
     */
    private String isoffdutyfreecheck;

    /**
     * 
     */
    private Integer shiftpersonnelruleid;

    /**
     * 
     */
    private String worktime;

    /**
     * 
     */
    private Integer subcompanyid;

    /**
     * 
     */
    private String isenablenextday;

    /**
     * 
     */
    private String isdelete;

    /**
     * 
     */
    private String uuid;

    /**
     * 
     */
    private String color;

    /**
     * 
     */
    private String cardremind;

    /**
     * 
     */
    private String cardremofsignin;

    /**
     * 
     */
    private Integer minsbeforesignin;

    /**
     * 
     */
    private String cardremofsignout;

    /**
     * 
     */
    private Integer minsaftersignout;

    /**
     * 
     */
    private Integer remindmode;

    /**
     * 
     */
    private String remindonpc;

    /**
     * 
     */
    private String halfcalrule;

    /**
     * 
     */
    private String halfcalpoint;

    /**
     * 
     */
    private String halfcalpoint2cross;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}