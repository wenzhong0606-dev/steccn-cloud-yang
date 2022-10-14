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

    Integer STATE_ONE = 1;

    Integer STATE_ZERO = 0;

    Integer STATE_TWO = 2;

    Integer STATE_THREE = 3;

    Integer STATE_FOUR = 4;

    Integer STATE_FIVE = 5;

    Integer STATE_SIX = 6;

    Integer STATE_SEVEN = 7;

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

}
