package com.steccn.pms.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 质量管理查找客户信息
 *
 * @author AndrewLuo
 * @date 2023/05/18
 */
@Data
public class QmsCustomerDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 产线id
     */
    private Integer productLineId;
    /**
     * 订单名字
     */
    private String productOrder;
    /**
     * 2）仅当手动输入生产订单号后校验为有效单号且该订单上的客户非空，预填该客户（若字段已填值，覆盖已填值）
     *
     * 3）仅当生产产线的值更新且值非空且检验类型为首检、巡检时，查询生产管理系统当前产线、当前时间执行中的生产订单的“客户”属性，若非空，系统自动带出该值（若字段已填值，覆盖已填值）
     *
     * 4）仅当生产产线/缺陷记录时段的值更新且值非空且检验类型为在线检时，查询生产管理系统当前产线、缺陷记录时段所属的生产订单的“客户”属性，若非空，系统自动带出该值（若字段已填值，覆盖已填值）
     */
    @NotNull(message = "类型不能为空")
    @Min(value = 2,message = "只能传2，3，4")
    @Max(value = 4,message = "只能传2，3，4")
    private Integer type;
    /**
     * 时间点
     */
    private LocalDateTime timeSpot;
}
