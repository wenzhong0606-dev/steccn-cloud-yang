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
     * 消息中心-大类别枚举
     */
    @Getter
    @AllArgsConstructor
    enum MsgCategoryEnum {
        All(-1, "全部"),
        TO_BE_DO(0, "待办待阅"),
        SYS_REMIND(1, "系统提醒");
        private Integer code;
        private String name;
    }

    /**
     * 消息中心-类别枚举
     */
    @Getter
    @AllArgsConstructor
    enum MsgTypeEnum {
        PURCHASE_APPLY(1, "采购申请审批", MsgCategoryEnum.TO_BE_DO.code),
        RECIPE_VERSION(2, "配方版本更新", MsgCategoryEnum.SYS_REMIND.code),
        SPC_WARN(3, "SPC超标告警", MsgCategoryEnum.SYS_REMIND.code);

        private Integer code;
        private String name;
        private Integer category;
    }
}
