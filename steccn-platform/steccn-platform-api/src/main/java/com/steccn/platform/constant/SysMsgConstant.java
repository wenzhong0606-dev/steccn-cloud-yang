package com.steccn.platform.constant;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yequan
 * @desc 消息中心常量
 */
@ApiModel("消息中心常量")
public interface SysMsgConstant {
    /**
     * 待办待阅
     */
    Integer TO_BE_DO = 0;

    /**
     * 消息提醒
     */
    Integer SYS_REMIND = 1;


    @Getter
    @AllArgsConstructor
    enum MsgTypeEnum {
        PURCHASE_APPLY(1, "采购申请审批", TO_BE_DO),
        RECIPE_VERSION(2, "配方版本更新", SYS_REMIND),
        SPC_WARN(3, "SPC超标告警", SYS_REMIND);

        private Integer code;
        private String name;
        private Integer category;
    }
}
