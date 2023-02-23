package com.steccn.pms.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @author W10
 * @desc 生产管理的常量接口
 */
public interface PmManagerConstant {

    /**
     * 测量单位
     */
    String MEASURE_UNIT = "measure_unit";

    /**
     * 单位转换
     */
    String MEASURE_UNIT_PIECE = "片";
    String MEASURE_UNIT_TON ="ton";
    String MEASURE_UNIT_SM ="m²";

    /**
     * 数字
     */
    String NUM_0 = "0";
    String NUM_1 = "1";
    String NUM_2 = "2";
    String NUM_3 = "3";
    String NUM_4 = "4";
    String NUM_5 = "5";
    String NUM_6 = "6";
    String NUM_7 = "7";
    String NUM_8 = "8";
    String NUM_9 = "9";
    String NUM_10 = "10";
    String NUM_11 = "11";
    String NUM_12 = "12";
    String NUM_13 = "13";
    String NUM_14 = "14";
    String NUM_15 = "15";
    String NUM_16 = "16";
    String NUM_17 = "17";
    String NUM_18 = "18";
    String NUM_19 = "19";
    String NUM_20 = "20";
    String NUM_21 = "21";
    String NUM_22 = "22";
    String NUM_23 = "23";
    String NUM_24 = "24";
    String NUM_25 = "25";
    String NUM_26 = "26";
    String NUM_27 = "27";
    String NUM_28 = "28";
    String NUM_29 = "29";
    String NUM_30 = "30";
    String NUM_31 = "31";
    String NUM_100 = "100";
    String NUM_1000 = "1000";

    String RECIPE_STR = "0000";

    /**
     * mrp 类型
     */
    String MRP_TYPE = "pms_mrp_type";

    /**
     * mrp库存
     */
    String MRP_INVENTORY = "mrp_inventory";

    /**
     * mrp 安全库存
     */
    String MRP_SAFETY_INVENTORY = "mrp_safety_inventory";

    /**
     * mrp 计划订单
     */
    String MRP_PLANNED_ORDER = "mrp_planned_order";

    /**
     * 采购申请
     */
    String MRP_PURCHASE_REQUEST = "mrp_purchase_request";

    /**
     * 采购订单
     */
    String MRP_PURCHASE_ORDER = "mrp_purchase_order";

    /**
     * 销售订单
     */
    String MRP_SALES_ORDER = "mrp_sales_order";

    /**
     * 独立需求
     */
    String MRP_INDEPENDENT_DEMAND = "mrp_independent_demand";

    /**
     * 生产需求
     */
    String MRP_MANUFACTURE_REQUIRMENT = "mrp_manufacture_requirment";

    /**
     * 生产订单
     */
    String MRP_PRODUCE_ORDER ="mrp_produce_order";

    /**
     * 班组
     */
    String SYS_TEAM = "sys_team";

    /**
     * 领料类型
     */
    String MATERIAL_REQUISITION =  "material_requisition";

    /**
     * 生产领料
     */
    String PRODUCE_PICK =  "produce_pick";
    /**
     * 班次
     */
    String SYS_SHIFT = "sys_shift";

    String FILE_EXTENSION_XLSX = ".xlsx";

    /**
     * 工位code
     */
    String STATE_13 = "13";

    /**
     * 产线code
     */
    String STATE_9 = "9";

    /**
     * 车间code
     */
    String STATE_8 = "8";

    /**
     * 单位T
     */
    String UNIT_T = "T";

    /**
     * 单位类型
     */
    String ENERGY_CONSUME = "energy_consume";

    /**
     * 领料单状态
     */
    String MATERIAL_REQUISITION_STATUS = "material_requisition_bill_status";
    /**
     * 已创建
     */
    String MRBS_CREATED = "mrbs_created";

    /**
     * 审批中
     */
    String MRBS_APPROVAL = "mrbs_approval";

    /**
     * 审批通过
     */
    String MRBS_APPROVAL_PASS = "mrbs_approval_pass";

    /**
     * 审批未通过
     */
    String MRBS_APPROVAL_FAIL = "mrbs_approval_fail";

    /**
     * 已删除
     */
    String MRBS_DELETE = "mrbs_delete";

    /**
     * 已完成
     */
    String MRBS_FINISH = "mrbs_finish";

    /**
     * 类型编码-工序类型
     */
    String PROCESS_TYPE = "process_type";

    /**
     * 计划数量
     */
    String PLAN_NUM = "计划数量";

    /**
     * 完工数量
     */
    String FINISH_NUM = "完工数量";

    /**
     * 入库数量
     */
    String RECEIPT_NUM = "入库数量";

    /**
     * 系统版本
     */
    String sysVersion = "2023-01-0000";

    /**
     * 配方超级管理员权限
     */
    String RECIPE_ROLE_SYSTEM = "recipeSystem";

    /**
     * 配方管理员权限
     */
    String RECIPE_ROLE_ADMIN = "recipeAdmin";

    /**
     * 配方管理查看
     */
    String RECIPE_PERMISSION_VIEW = "pms:recipe:view";

    /**
     * 入库出库凭证状态
     */
    @Getter
    @AllArgsConstructor
    enum InVouchEnum {
        CREATE(0, "已创建"), FINISH(1, "已完成");

        private final Integer code;

        private final String name;
    }


    /**
     * 箱状态0:已完工 1:待入库 2：已入库
     */
    @Getter
    @AllArgsConstructor
    enum CaseStateEnum {
        FINISH(0, "已完工"), PREPARE(1, "待入库"), ALREADY(2, "已入库");

        private final Integer code;

        private final String name;
    }

    /**
     * 状态:0 ：已创建  1：已下单  2：执行中 3：暂停中  4：已暂停 5：完成中 6：已完成 7：已关闭 ）
     */
    @Getter
    @AllArgsConstructor
    enum OrderStateEnum {
        ZERO(0, "已创建"),
        ONE(1, "已下单"),
        TWO(2, "执行中"),
        THREE(3, "暂停中"),
        FOUR(4, "已暂停"),
        FIVE(5, "完成中"),
        SIX(6, "已完成"),
        SEVEN(7, "已关闭");

        private final Integer code;

        private final String name;
    }

    /**
     * 对比类型
     */
    @Getter
    @AllArgsConstructor
    enum QtChartCompareEnum {
        //班次 产线 车间 缺陷类别
        ALL(-1, "不对比"),
        WORK(0, "班次"),
        PRODUCTLINE(1, "产线"),
        WORKSHOP(2, "车间"),
        DEFECTCATEGORY(3, "缺陷类别"),
        SAMPLINGTIME(4, "取样日期"),
        MATERIALWORKSHOP(5, "物料使用车间"),
        SUPPLIER(6, "供应商"),
        MATERIAL(7, "物料名称");

        private Integer code;
        private String name;

        public static String getNameByCode(Integer code) {
            for (QtChartCompareEnum value : QtChartCompareEnum.values()) {
                if (value.getCode().equals(code)) {
                    return value.getName();
                }
            }
            return null;
        }
    }
}
