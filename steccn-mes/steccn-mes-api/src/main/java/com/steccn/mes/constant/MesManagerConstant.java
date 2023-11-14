package com.steccn.mes.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.compress.utils.Sets;

import java.util.Set;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/07/26
 */
public interface MesManagerConstant {
    /**
     * 所有的客户放在这个下面
     */
    String customerGroup = "CUCC8013";
    /**
     * 所有供应商在这个下面
     */
    String supplierGroup = "CUCC1012";

    /**
     * 所有仓库在这个下面
     */
    String STOCK_GROUP = "STOCKGROUP";

    Integer NUM_0 = 0;
    Integer NUM_1 = 1;
    Integer NUM_30 = 30;
    String ERP = "erp";

    /**
     * 客户类型
     */
    String CUSTOMER_TYPE = "7";

    /**
     * 供应商类型
     */
    String SUPPLIER_TYPE = "11";

    /**
     * 仓库类型
     */
    String STOCK_TYPE = "12";

    /**
     * 温度点检项
     */
    @Getter
    @AllArgsConstructor
    enum TemperatureItemEnum {
        ONE(1658273843689275392L, "板中温度","bzTemp"),
        TWO(1658273797971361792L, "上辊北温度","sgbTemp"),
        THREE(1658273764500815872L, "上辊中温度","sgzTemp"),
        FOUR(1658273731030269952L, "上辊南温度","sgnTemp"),
        FIVE(1658273173586296832L, "下辊表面温度","xgnTemp"),
        SIX(1658272704994459648L,"辊前玻璃液温度","gqblTemp"),
        SEVEN(1658272613550243840L,"流溢口玻璃液温度","lakblTemp");

        private final Long id;

        private final String itemName;

        private final String code;

        /**
         * 所有的id
         *
         * @return
         */
        public static Set<Long> all() {
            Set<Long> all = Sets.newHashSet();
            for (TemperatureItemEnum value : TemperatureItemEnum.values()) {
                all.add(value.getId());
            }
            return all;
        }

        /**
         * 根据id获取名称
         * @param id
         * @return
         */
        public static String getItemNameId(Long id) {
            for (TemperatureItemEnum value : TemperatureItemEnum.values()) {
                if (value.getId().equals(id)) {
                    return value.getItemName();
                }
            }
            return null;
        }

        public static String getItemCodeById(Long id) {
            for (TemperatureItemEnum value : TemperatureItemEnum.values()) {
                if (value.getId().equals(id)) {
                    return value.getCode();
                }
            }
            return null;
        }
    }
}
