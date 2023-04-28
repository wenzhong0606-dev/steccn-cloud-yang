package com.steccn.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yequan
 * @desc 车间产线转换
 * @date 2023/4/28
 */
public class WorkLineConvertUtil {

    /**
     * 车间产线转换
     *
     * @param fullName 组织机构名称
     * @return
     */
    public static Integer[] convert(String fullName) {
        Integer workShop = WorkShopEnum.WORK_SHOP_OTHER.getCode();
        Integer productLine = ProductLineEnum.PRODUCT_LINE_OTHER.getCode();

        for (WorkShopEnum work : WorkShopEnum.values()) {
            if (fullName.contains(work.getName())) {
                workShop = work.getCode();
                break;
            }
        }
        for (ProductLineEnum line : ProductLineEnum.values()) {
            if (fullName.contains(line.getName())) {
                productLine = line.getCode();
                break;
            }
        }
        return new Integer[]{workShop, productLine};
    }

    @Getter
    @AllArgsConstructor
    public enum WorkShopEnum {
        WORK_SHOP_8(8, "八车间"),
        WORK_SHOP_7(7, "七车间"),
        WORK_SHOP_6(6, "六车间"),
        WORK_SHOP_5(5, "五车间"),
        WORK_SHOP_4(4, "四车间"),
        WORK_SHOP_3(3, "三车间"),
        WORK_SHOP_2(2, "二车间"),
        WORK_SHOP_1(1, "一车间"),
        WORK_SHOP_OTHER(Integer.MAX_VALUE, "其它车间");

        private Integer code;
        private String name;
    }

    @Getter
    @AllArgsConstructor
    public enum ProductLineEnum {
        PRODUCT_LINE_8(8, "八产线"),
        PRODUCT_LINE_7(7, "七产线"),
        PRODUCT_LINE_6(6, "六产线"),
        PRODUCT_LINE_5(5, "五产线"),
        PRODUCT_LINE_4(4, "四产线"),
        PRODUCT_LINE_3(3, "三产线"),
        PRODUCT_LINE_2(2, "二产线"),
        PRODUCT_LINE_1(1, "一产线"),
        PRODUCT_LINE_OTHER(Integer.MAX_VALUE, "其它产线");

        private Integer code;
        private String name;
    }
}
