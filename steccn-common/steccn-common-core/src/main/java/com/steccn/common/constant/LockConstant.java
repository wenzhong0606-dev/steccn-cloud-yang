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
     * 冲销出库
     */
    String WMS_CHARGE_AGAINST ="WMS_CHARGE_AGAINST";


    /**
     * 仪表读数归档 电费归档
     */
    String DW_METER_READ_SCHEDULED_TASK = "DW_METER_READ_SCHEDULED_TASK";
    String DW_ELECTRIC_MONEY_SCHEDULED_TASK = "DW_ELECTRIC_MONEY_SCHEDULED_TASK";
    String DW_READ_SCHEDULED_TASK = "DW_READ_SCHEDULED_TASK";
    String DW_HEAT_POWER_TASK = "DW_HEAT_POWER_TASK";
    String DW_METER_READ_DISTANT_TASK = "DW_METER_READ_DISTANT_TASK";

    /**
     * 单耗告警定时任务
     */
    String UNIT_CONSUME_ALARM_SCHEDULED_TASK = "UNIT_CONSUME_ALARM_SCHEDULED_TASK";

    /**
     * 仓储调拨单
     */
    String TABLE_WMS_TRANSFER_SLIP = "TABLE_WMS_TRANSFER_SLIP";

    /**
     * 仓储出库单
     */
    String TABLE_WMS_DELIVERY_RECEIPT = "TABLE_WMS_DELIVERY_RECEIPT";

    /**
     * 仓储入库单
     */
    String TABLE_WMS_STORAGE_RECEIPT = "TABLE_WMS_STORAGE_RECEIPT";

    /**
     * 仓储物流单
     */
    String TABLE_WMS_LOGISTICS_ORDER = "TABLE_WMS_LOGISTICS_ORDER";

    /**
     * 生成箱号
     */
    String TABLE_PMS_BARCODE_PRINT = "TABLE_PMS_BARCODE_PRINT";

    /**
     * 生成入库单号
     */
    String TABLE_PMS_COMPLETE_STORAGE = "TABLE_PMS_COMPLETE_STORAGE";

    /**
     * 更新用量,更新含水率
     */
    String TABLE_PMS_RECIPE = "TABLE_PMS_RECIPE";

    /**
     * 更新拉引量
     */
    String TABLE_PMS_PULL_FORCE = "TABLE_PMS_PULL_FORCE";

    /**
     * SPC告警锁
     */
    String SPC_WARN_HOUR = "SPC_WARN_HOUR";
    String SPC_WARN_DAY = "SPC_WARN_DAY";
    String SPC_WARN_WEEK = "SPC_WARN_WEEK";
    String SPC_WARN_MONTH = "SPC_WARN_MONTH";
    String SPC_WARN_SEASON = "SPC_WARN_SEASON";
    String SPC_WARN_YEAR = "SPC_WARN_YEAR";

    /**
     * 客户物料提交操作
     */
    String CUSTOMER_MATERIAL = "CUSTOMER_MATERIAL";

    /**
     * 客诉处理
     */
    String QT_CUSTOMER_COMPLAIN = "QT_CUSTOMER_COMPLAIN";

    /**
     * 不合格品处理
     */
    String QT_DEFECT_PRODUCT = "QT_DEFECT_PRODUCT";

    /**
     * 整改记录
     */
    String QT_IMPROVE_RECORD = "QT_IMPROVE_RECORD";

    /**
     * 拉引量计算
     */
    String pullAmountCalculate = "pullAmountCalculate";
}

