package com.steccn.oa.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 
 * @TableName kq_group
 */
@TableName(value ="kq_group")
@Data
public class KqGroup implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private String groupname;

    /**
     * 
     */
    private Integer subcompanyid;

    /**
     * 
     */
    private Integer kqtype;

    /**
     * 
     */
    private String excludeid;

    /**
     * 
     */
    private Integer signintype;

    /**
     * 
     */
    private String ipscope;

    /**
     * 
     */
    private Integer locationcheck;

    /**
     * 
     */
    private Integer locationcheckscope;

    /**
     * 
     */
    private Integer wificheck;

    /**
     * 
     */
    private String weekday;

    /**
     * 
     */
    private String shiftcyclename;

    /**
     * 
     */
    private Integer shiftcycleday;

    /**
     * 
     */
    private String shiftcycleserialids;

    /**
     * 
     */
    private String serialids;

    /**
     * 
     */
    private String signstart;

    /**
     * 
     */
    private BigDecimal workhour;

    /**
     * 
     */
    private Integer outsidesign;

    /**
     * 
     */
    private Integer validity;

    /**
     * 
     */
    private String validityfromdate;

    /**
     * 
     */
    private String validityenddate;

    /**
     * 
     */
    private Integer isdelete;

    /**
     * 
     */
    private Integer excludecount;

    /**
     * 
     */
    private Integer locationfacecheck;

    /**
     * 
     */
    private Integer locationshowaddress;

    /**
     * 
     */
    private Integer wififacecheck;

    /**
     * 
     */
    private String ipscopeV4Pc;

    /**
     * 
     */
    private String ipscopeV4Em;

    /**
     * 
     */
    private String ipscopeV6Pc;

    /**
     * 
     */
    private String ipscopeV6Em;

    /**
     * 
     */
    private Integer autoCheckin;

    /**
     * 
     */
    private Integer autoCheckinBefore;

    /**
     * 
     */
    private Integer autoCheckinAfter;

    /**
     * 
     */
    private Integer autoCheckout;

    /**
     * 
     */
    private Integer autoCheckoutBefore;

    /**
     * 
     */
    private Integer autoCheckoutAfter;

    /**
     * 
     */
    private Integer calmethod;

    /**
     * 
     */
    private Integer locationfacechecktype;

    /**
     * 
     */
    private Integer wififacechecktype;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}