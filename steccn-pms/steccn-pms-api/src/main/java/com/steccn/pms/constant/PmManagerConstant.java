package com.steccn.pms.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author W10
 * @desc 生产管理的常量接口
 */
public interface PmManagerConstant {


    String MEASURE_UNIT = "measure_unit";

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

    String FILE_EXTENSION_XLSX = ".xlsx";

    /**
     * 工位code
     */
    String STATE_13 = "13";

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

}
