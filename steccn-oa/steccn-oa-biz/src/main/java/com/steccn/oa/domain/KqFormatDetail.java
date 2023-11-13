package com.steccn.oa.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName kq_format_detail
 */
@TableName(value ="kq_format_detail")
@Data
public class KqFormatDetail implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer resourceid;

    /**
     * 
     */
    private String kqdate;

    /**
     * 
     */
    private Integer serialnumber;

    /**
     * 
     */
    private Integer groupid;

    /**
     * 
     */
    private Integer serialid;

    /**
     * 
     */
    private String workbegindate;

    /**
     * 
     */
    private String workbegintime;

    /**
     * 
     */
    private String workenddate;

    /**
     * 
     */
    private String workendtime;

    /**
     * 
     */
    private Integer workmins;

    /**
     * 
     */
    private String signindate;

    /**
     * 
     */
    private String signintime;

    /**
     * 
     */
    private Integer signinid;

    /**
     * 
     */
    private String signoutdate;

    /**
     * 
     */
    private String signouttime;

    /**
     * 
     */
    private Integer signoutid;

    /**
     * 
     */
    private Integer attendancemins;

    /**
     * 
     */
    private Integer belatemins;

    /**
     * 
     */
    private Integer gravebelatemins;

    /**
     * 
     */
    private Integer leaveearlymins;

    /**
     * 
     */
    private Integer graveleaveearlymins;

    /**
     * 
     */
    private Integer absenteeismmins;

    /**
     * 
     */
    private Integer leavemins;

    /**
     * 
     */
    private Integer evectionmins;

    /**
     * 
     */
    private Integer outmins;

    /**
     * 
     */
    private Integer forgotcheckmins;

    /**
     * 
     */
    private Integer signmins;

    /**
     * 
     */
    private String leaveinfo;

    /**
     * 
     */
    private Integer forgotbeginworkcheckmins;

    /**
     * 
     */
    private String otherinfo;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}