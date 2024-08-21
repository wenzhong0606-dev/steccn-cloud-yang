package com.steccn.dp.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 
 * @TableName point_site_map
 */
@TableName(value ="point_site_map")
@Data
public class PointSiteMap implements Serializable {
    /**
     * 
     */
    private String id;

    /**
     * 
     */
    private String type;

    /**
     * 
     */
    private String site;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}