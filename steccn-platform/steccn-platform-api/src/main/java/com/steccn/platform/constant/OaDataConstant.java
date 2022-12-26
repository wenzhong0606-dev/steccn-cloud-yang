package com.steccn.platform.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Oa数据常量
 *
 * @author AndrewLuo
 * @date 2022/12/26
 */
public interface OaDataConstant {

    String STR_CLIENT_ID = "client-id";

    @Getter
    @AllArgsConstructor
    enum Oa_Data_Type {
        PROCESS(1, "新流程推送接口"),
        MESSAGE(2, "新消息推送接口"),
        NOTIFY(3, "流程审批返回信息接口");

        private Integer code;
        private String name;
    }
}
