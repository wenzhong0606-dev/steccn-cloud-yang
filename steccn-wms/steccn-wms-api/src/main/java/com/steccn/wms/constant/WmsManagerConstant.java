package com.steccn.wms.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @desc 仓储管理的常量接口
 */
public interface WmsManagerConstant {

    /**
     * one
     */
    String CODE_ONE = "1";

    String CODE_TWO = "2";

    /**
     * 采购类型
     */
    String WAREHOUSING_TYPE = "warehousing_type";

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


}
