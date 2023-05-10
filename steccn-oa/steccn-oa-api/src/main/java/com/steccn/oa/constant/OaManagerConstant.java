package com.steccn.oa.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OA数据常量
 */
public interface OaManagerConstant {


    @Getter
    @AllArgsConstructor
    enum OaWorkFlow {
        CUSTOMER_COMPLAIN("151","客诉处理审批"),
        DEFECT_PRODUCT("152","不合格品处理审批"),
        IMPROVER_APPROVE("153", "整改启动审批"),
        IMPROVER_CLOSEOUT("154", "整改结案审批");

        private String id;
        private String name;
    }

    /**
     * Oa班组映射
     */
    @Getter
    @AllArgsConstructor
    enum OaTeamGroupMapping {
        //班次 产线 车间 缺陷类别
        A("车间倒班-甲班", "甲班"),
        B("车间倒班-乙班", "乙班"),
        C("车间倒班-丙班", "丙班");

        private String OaName;
        private String name;

        public static String getNameByOaName(String oaName) {
            for (OaTeamGroupMapping value : OaTeamGroupMapping.values()) {
                if (value.getOaName().equals(oaName)) {
                    return value.getName();
                }
            }
            return null;
        }
    }
}
