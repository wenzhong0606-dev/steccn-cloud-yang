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

    String FILE_EXTENSION_XLSX = ".xlsx";

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
        FIVE(5,"完成中"),
        SIX(6,"已完成"),
        SEVEN(7,"已关闭");

        private final Integer code;

        private final String name;
    }

}
