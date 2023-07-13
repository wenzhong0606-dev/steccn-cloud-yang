package com.steccn.oa.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName kq_group_shiftcycle
 */
@TableName(value ="kq_group_shiftcycle")
@Data
public class KqGroupShiftcycle implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

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
    private Integer groupid;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}