package com.steccn.common.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @desc 规格枚举  产品属性(0镀膜无印,1双膜无印,2无膜无印,3无膜丝印
 */
@Getter
@RequiredArgsConstructor
public enum ProductPropertyEnum {

    COAT_NO_PRINT(0, "镀膜无印"),
    DOUBLE_FILM_NO_PRINT(1, "双膜无印"),
    NO_FILM_NO_PRINT(2, "无膜无印"),
    NO_FILM_SCREEN_PRINT(3, "无膜丝印");


    private final Integer id;
    private final String name;

    //根据code查找值
    public static ProductPropertyEnum property(Integer id) {
        for (ProductPropertyEnum value : ProductPropertyEnum.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }

}
