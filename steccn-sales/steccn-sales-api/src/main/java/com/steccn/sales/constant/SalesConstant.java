package com.steccn.sales.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/11/29
 */
public interface SalesConstant {

    /**
     * 包装类型映射
     */
    @Getter
    @AllArgsConstructor
    enum salesPackTypeMapping {
        //班次 产线 车间 缺陷类别
        WOOD("wood", "木托"),
        IRON("iron", "铁托"),
        U("u", "U型架"),
        L("l", "L架"),
        BOX("box", "木箱");

        private String code;
        private String name;

        public static String getNameByCode(String code) {
            for (salesPackTypeMapping value : salesPackTypeMapping.values()) {
                if (value.getCode().equals(code)) {
                    return value.getName();
                }
            }
            return null;
        }
    }
}
