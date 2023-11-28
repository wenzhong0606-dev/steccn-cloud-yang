package com.steccn.dp.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;

/**
 * tagId包含Raw_YL.ZHM.fact_weigh的数据
 * @TableName ads_opc_ua_all3
 */
@TableName(value ="ads_opc_ua_all3")
@Data
public class AdsOpcUaAll3 implements Serializable {
    /**
     * tagId
     */
    @TableId
    private String tagId;

    /**
     * 毫秒级时间戳？
     */
    private String servertimems;

    /**
     * nodeId
     */
    private String nodeid;

    /**
     * kafka主题
     */
    private String kafkatopic;

    /**
     * opcPath路径
     */
    private String opcpath;

    /**
     * 秒级时间
     */
    private LocalDateTime servertime;

    /**
     * 
     */
    private BigDecimal value;

    /**
     * value的string表示
     */
    private String valueasstring;

    /**
     * 
     */
    private Integer statuscode;

    /**
     * 
     */
    private String valuetype;

    /**
     * 删除标记：默认0不删除
     */
    @TableLogic
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}