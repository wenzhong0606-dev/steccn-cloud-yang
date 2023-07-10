package com.steccn.oa.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName kq_shiftonoffworksections
 */
@TableName(value ="kq_shiftonoffworksections")
@Data
public class KqShiftonoffworksections implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 
     */
    private Integer serialid;

    /**
     * 
     */
    private String across;

    /**
     * 
     */
    private String beginmin;

    /**
     * 
     */
    private String endmin;

    /**
     * 
     */
    private String times;

    /**
     * 
     */
    private String onoffworktype;

    /**
     * 
     */
    private String isdelete;

    /**
     * 
     */
    private String mins;

    /**
     * 
     */
    private String record;

    /**
     * 
     */
    private String minsNext;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}