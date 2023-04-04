package com.steccn.oa.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName kq_groupmember
 */
@TableName(value ="kq_groupmember")
@Data
public class KqGroupmember implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private Integer type;

    /**
     * 
     */
    private Integer typevalue;

    /**
     * 
     */
    private Integer alllevel;

    /**
     * 
     */
    private Integer jobtitlelevel;

    /**
     * 
     */
    private Integer jobtitlelevelvalue;

    /**
     * 
     */
    private Integer seclevel;

    /**
     * 
     */
    private Integer seclevelto;

    /**
     * 
     */
    private Integer groupid;

    /**
     * 
     */
    private Integer isdelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}