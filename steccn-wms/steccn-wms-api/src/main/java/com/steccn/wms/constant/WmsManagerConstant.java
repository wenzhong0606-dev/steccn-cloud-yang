package com.steccn.wms.constant;

import com.steccn.pms.constant.PmManagerConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @desc 仓储管理的常量接口
 */
public interface WmsManagerConstant {


    String Code_ZERO = "0";

    String VARIATION_TYPE= "variation_type";

    /**
     * one
     */
    String CODE_ONE = "1";

    String CODE_TWO = "2";


    String CODE_FOUR = "4";

    /**
     * 采购类型
     */
    String WAREHOUSING_TYPE = "warehousing_type";

    /**
     * 出库字典类型
     */
    String OUTBOUND_TYPE = "outbound_type";

    String JSON_SUCCESS = "success";

    String JSON_MESSAGE = "message";

    /**
     * 入库单类型1
     */
    String RECEIPT_TYPE_1 = "生产完工入库";

    /**
     * 入库单类型2
     */
    String RECEIPT_TYPE_2 = "采购入库";


    /**
     * 是否
     */
    @Getter
    @AllArgsConstructor
    enum YesOrNo {
        NO(0, "否"),
        YES(1, "是");

        private Integer code;
        private String name;

    }


    /**
     * 检验值
     */
    @Getter
    @AllArgsConstructor
    enum CheckScope {
        WAREHOUSE(0, "仓库"),
        SHELVES(1, "货架"),
        MATERIALCLASSIFICATION(2, "物料分类"),
        MATERIAL(3, "物料");

        private Integer code;
        private String name;

    }

    /**
     * 状态:0 ：已创建  1：已确认  2：部分确认 3：已完成 4:已删除 ）
     */
    @Getter
    @AllArgsConstructor
    enum OrderStateEnum {
        ZERO(0, "已创建"),
        ONE(1, "已确认"),
        TWO(2, "部分确认"),
        THREE(3, "已完成"),
        FOUR(4, "已删除"),
        FIVE(5,"已冲销");

        private final Integer code;

        private final String name;
    }


    @Getter
    @AllArgsConstructor
    enum CheckOrderStateEnum {
        ZERO(0, "已创建"),
        ONE(1, "执行中"),
        TWO(2, "已完成"),
        THREE(3, "已关闭"),
        FOUR(4, "已归档");

        private final Integer code;

        private final String name;
    }
    @Getter
    @AllArgsConstructor
    enum CheckOrderInfoStateEnum {
        ZERO(0, "未执行"),
        ONE(1, "已执行");

        private final Integer code;

        private final String name;
    }
    /**
     * 状态:0:加  1：减  ）
     */
    @Getter
    @AllArgsConstructor
    enum InventoryEnum {
        ZERO(0, "加"),
        ONE(1, "减"),
        TWO(2, "先减后加"),
        THREE(3, "调整总数量");

        private final Integer code;

        private final String name;
    }


    /**
     * 状态:0 ：已创建  1：运输中  2：已签收 ）
     */
    @Getter
    @AllArgsConstructor
    enum LogisticsStateEnum {
        ZERO(0, "已创建"),
        ONE(1, "运输中"),
        TWO(2, "已签收"),
        THREE(3, "已删除");


        /**
         * 编码
         */
        private final Integer code;

        /**
         * 名称
         */
        private final String name;
    }


    /**
     * 收发货类型（0：收 1：发）
     */
    @Getter
    @AllArgsConstructor
    enum LogisticsTypeEnum {
        ZERO(0, "收货"),
        ONE(1, "发货");

        private final Integer code;

        private final String name;
    }

    /**
     * 销售送货单
     */
    @Getter
    @AllArgsConstructor
    enum SalesOrderStateEnum {
        SHIPPED("SHIPPED","已发货"),
        CANCELLED("CANCELLED", "已取消"),
        CLOSED("CLOSED", "已关闭"),
        DEPRECATED("DEPRECATED","已作废");

        private final String code;

        private final String name;
    }

    /**
     * 变动类型
     */
    @Getter
    @AllArgsConstructor
    enum variableTypeEnum{
        TYPE_0("0","期初入库"),
        TYPE_1("1","创建完工入库"),
        TYPE_2("2","确认完工入库"),
        TYPE_3("3","反向确认完工入库"),
        TYPE_4("4","采购入库"),
        TYPE_5("5","冲销入库"),
        TYPE_6("6","销售退货入库"),
        TYPE_7("7","生产退料入库"),
        TYPE_8("8","其他入库"),
        TYPE_9("9","调拨入库"),
        TYPE_10("10","盘盈入库"),
        TYPE_11("11","期初出库"),
        TYPE_12("12","生产领料出库"),
        TYPE_13("13","采购退货出库"),
        TYPE_14("14","销售发货出库"),
        TYPE_15("15","冲销出库"),
        TYPE_16("16","其他出库"),
        TYPE_17("17","调拨出库"),
        TYPE_18("18","盘亏出库");

        private final String type;

        private final String name;


        public static String getTypeByName(String name) {
            for (WmsManagerConstant.variableTypeEnum value : WmsManagerConstant.variableTypeEnum.values()) {
                if (value.getName().equals(name)) {
                    return value.getType();
                }
            }
            return null;
        }
    }

}
