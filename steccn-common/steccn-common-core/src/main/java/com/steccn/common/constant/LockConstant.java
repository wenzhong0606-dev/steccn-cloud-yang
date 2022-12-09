package com.steccn.common.constant;

/**
 * @author yequan
 * @desc 分布式锁常量
 */
public interface LockConstant {

    /**
     * 表报工
     */
    String TABLE_PMS_REPORT_WORK = "TABLE_PMS_REPORT_WORK";


    /**
     * 计划下达
     */
    String PLAN_ORDER ="PLAN_ORDER";


    /**
     * 生产订单新增
     */
    String TABLE_PMS_PRODUCE_ORDER = "TABLE_PMS_PRODUCE_ORDER";

    /**
     * 纠偏定时任务
     */
    String READ_CORRECT_SCHEDULED_TASK = "READ_CORRECT_SCHEDULED_TASK";

    /**
     * 仓储调拨单
     */
    String TABLE_WMS_TRANSFER_SLIP = "TABLE_WMS_TRANSFER_SLIP";

    /**
     * 仓储出库单
     */
    String TABLE_WMS_DELIVERY_RECEIPT = "TABLE_WMS_DELIVERY_RECEIPT";
}
