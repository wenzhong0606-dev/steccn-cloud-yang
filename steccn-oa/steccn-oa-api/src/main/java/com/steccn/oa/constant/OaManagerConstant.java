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
}
