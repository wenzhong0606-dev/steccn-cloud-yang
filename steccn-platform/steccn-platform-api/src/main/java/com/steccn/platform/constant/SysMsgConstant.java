package com.steccn.platform.constant;

import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


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
        ALL(-1, "全部"),
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
        PURCHASE_APPLY(1, "采购申请审批", MsgCategoryEnum.TO_BE_DO.code, "",0),
        RECIPE_VERSION(2, "配方版本更新", MsgCategoryEnum.SYS_REMIND.code, "%s更新了配方，最新版本为%s",1),
        SPC_WARN(3, "SPC超标告警", MsgCategoryEnum.SYS_REMIND.code, "SPC名称: %s \r\n物料名称: %s\r\n检验项: %s",0),
        RECIPE_REFRESH(4, "配方版本刷新通知", MsgCategoryEnum.SYS_REMIND.code, "",0),
        CUSTOMER_COMPLAIN(5,"客诉待处理",MsgCategoryEnum.SYS_REMIND.code,"客户: %s \r\n投诉类型: %s\r\n客诉标题: %s",0),
        DEFECT_PRODUCT(6,"不合格品待处理",MsgCategoryEnum.SYS_REMIND.code,"标题: %s \r\n问题描述: %s\r\n检验类型: %s",0),
        IMPROVE_RECORD_PENDING(7,"整改待处理",MsgCategoryEnum.SYS_REMIND.code,"整改标题: %s \r\n创建人: %s\r\n",0),
        IMPROVE_RECORD_SEND_BACK(8,"整改退回",MsgCategoryEnum.SYS_REMIND.code,"整改标题: %s \r\n创建人: %s\r\n",0),
        CUSTOMER_COMPLAIN_APPROVE(9,"客诉审批通过",MsgCategoryEnum.SYS_REMIND.code,"客户: %s \r\n投诉类型: %s\r\n客诉标题: %s",0),
        SEND_BACK(10,"流程实例标题",MsgCategoryEnum.TO_BE_DO.code,"发起人：%s",0),
        DEFECT_PRODUCT_APPROVE(11,"不合格品处理通过",MsgCategoryEnum.SYS_REMIND.code, "标题： %s \r\n问题描述： %s \r\n 检验类型：%s",0),
        IMPROVE_RECORD_APPROVE(12,"整改审批结案",MsgCategoryEnum.TO_BE_DO.code,"整改标题: %s \r\n创建人: %s\r\n",0),
        IMPROVE_RECORD_APPROVE_PASS(13,"整改审批结案通过",MsgCategoryEnum.SYS_REMIND.code,"整改标题: %s \r\n创建人: %s\r\n",0),
        SPC_SERIAL_UP_WARN(14, "SPC连续上升告警", MsgCategoryEnum.SYS_REMIND.code, "SPC名称: %s \r\n物料名称: %s\r\n检验项: %s",0),
        SPC_SERIAL_DOWN_WARN(15, "SPC连续下降告警", MsgCategoryEnum.SYS_REMIND.code, "SPC名称: %s \r\n物料名称: %s\r\n检验项: %s",0),
        BATCH_CHECK_UPDATE(16,"配料抽检日报表更新",MsgCategoryEnum.SYS_REMIND.code,"%s更新了配料抽检日报表",1),
        UNIT_CONSUME_ALARM(17,"能源单耗超标",MsgCategoryEnum.SYS_REMIND.code,"%s日单耗%s,超过%s",0);


        private Integer code;
        private String title;
        private Integer category;
        private String template;
        /**
         * 强提醒标识 0:不提醒 1:提醒
         */
        private Integer warnFlag;

        /**
         * 根据大类别 查询小分类id集合
         *
         * @param code 小类别id
         * @return
         */
        public static Integer category(Integer code) {
            for (MsgTypeEnum value : MsgTypeEnum.values()) {
                //如果类别相同，或者类别为全部
                if (value.getCode().equals(code)) {
                    return value.getCategory();
                }
            }
            return null;
        }

        /**
         * 根据大类别 查询小分类id集合
         *
         * @param category 大类别id
         * @return
         */
        public static List<Integer> codeList(Integer category) {
            List<Integer> resList = Lists.newArrayList();
            for (MsgTypeEnum value : MsgTypeEnum.values()) {
                //如果类别相同，或者类别为全部
                if (value.getCategory().equals(category) || SysMsgConstant.MsgCategoryEnum.ALL.getCode().equals(category)) {
                    resList.add(value.getCode());
                }
            }
            return resList;
        }
    }
}
