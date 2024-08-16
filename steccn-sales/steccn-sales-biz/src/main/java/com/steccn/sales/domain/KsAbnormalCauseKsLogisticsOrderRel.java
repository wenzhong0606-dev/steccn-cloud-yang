package com.steccn.sales.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * RELATION BETWEEN ks_logistics_order AND ks_abnormal_cause
 * @TableName ks_abnormal_cause_ks_logistics_order_rel
 */
@TableName(value ="ks_abnormal_cause_ks_logistics_order_rel")
@Data
public class KsAbnormalCauseKsLogisticsOrderRel implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Integer ksLogisticsOrderId;

    /**
     * 
     */
    private Integer ksAbnormalCauseId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}