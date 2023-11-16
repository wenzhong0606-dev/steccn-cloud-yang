package com.steccn.wms.constant;

import com.steccn.pms.constant.PmManagerConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @desc 仓储管理的常量接口
 */
public interface WmsManagerConstant {


    String SERVER_CODE ="query_logistics";

    String Code_ZERO = "0";

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
     * 销售模块物流单状态
     */
    @Getter
    @AllArgsConstructor
    enum LogisticsBillState {
        DRAFT( "草稿"),
        RESERVED( "已预约"),
        SYN_SENT("已同步"),
        ALREADY_ENTERED("已入场"),
        OUT_OF_STORAGE("已出库");

        private String name;

    }


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
     * erp对应产线编码
     */
    @Getter
    @AllArgsConstructor
    enum ErpProductionLineCodeEnum{
        JP1("013402-03.01-01","基片一车间一产线"),
        JP2("013402-03.01-01","基片一车间二产线"),
        JG1("013402-03.02-01","深加工一车间一产线"),
        JG2("013402-03.02-02","深加工一车间二产线"),
        JG3("013402-03.02-03","深加工一车间三产线"),
        JG4("013402-03.02-04","深加工一车间四产线"),
        XJP1("013402-03.01-02","基片一产线"),
        XJP2("013402-03.01-02","基片二产线"),
        XJP3("013402-03.01-02","基片三产线"),
        XJP4("013402-03.01-02","基片四产线"),
        XJP5("013402-03.01-02","基片五产线"),
        XJP6("013402-03.01-02","基片六产线"),
        XJP7("013402-03.01-02","基片七产线"),
        XJP8("013402-03.01-02","基片八产线"),
        XJG1("013402-03.08","深加工一产线"),
        XJG2("013402-03.08","深加工二产线"),
        XJG3("013402-03.08","深加工三产线"),
        XJG4("013402-03.08","深加工四产线"),
        XJG5("013402-03.09","深加工五产线"),
        XJG6("013402-03.09","深加工六产线"),
        XJG7("013402-03.09","深加工七产线"),
        XJG8("013402-03.09","深加工八产线");

        private final String code;
        private final String name;

        public static String getCodeByName(String name) {
            for (WmsManagerConstant.ErpProductionLineCodeEnum value : WmsManagerConstant.ErpProductionLineCodeEnum.values()) {
                if (value.getName().equals(name)) {
                    return value.getCode();
                }
            }
            return null;
        }
    }

}
