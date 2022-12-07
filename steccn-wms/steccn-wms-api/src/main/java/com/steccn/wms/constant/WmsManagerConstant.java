package com.steccn.wms.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @desc 仓储管理的常量接口
 */
public interface WmsManagerConstant {


    /**
     * 检验值
     */
    @Getter
    @AllArgsConstructor
    enum CheckScope{
        WAREHOUSE(0,"仓库"),
        SHELVES(1,"货架"),
        MATERIALCLASSIFICATION(2,"物料分类"),
        MATERIAL(3,"物料");

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
        TWO(2,"部分确认"),
        THREE(3, "已完成"),
        FOUR(4, "已删除");

        private final Integer code;

        private final String name;
    }

}
