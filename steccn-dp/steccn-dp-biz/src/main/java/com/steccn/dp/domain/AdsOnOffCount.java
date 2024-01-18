package com.steccn.dp.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 对开关数据的计数统计
 * @TableName ads_on_off_count
 */
@TableName(value ="ads_on_off_count")
@Data
public class AdsOnOffCount implements Serializable {
    /**
     * tagId
     */
    @TableId
    private String tagId;

    /**
     * 中文说明
     */
    private String name;

    /**
     * 
     */
    private String value;

    /**
     * 
     */
    private Integer flag1;

    /**
     * 统计的日期
     */
    private String dt;

    /**
     * 产线
     */
    private String productLine;

    /**
     * 
     */
    private String note;

    /**
     * 统计开始时间和结束时间
     */
    private String startToEnd;

    /**
     * 统计数量
     */
    private Long valueCount;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}