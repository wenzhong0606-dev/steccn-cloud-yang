package com.steccn.websocket.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yequan
 * @desc websocket常量
 */
public interface WebsocketConstant {

    /**
     * websocket推送消息来源
     */
    @Getter
    @AllArgsConstructor
    enum WsPushTypeEnum {
        OA(0, "OA"),
        MSG_SYS_REMIND(1, "系统消息提醒");

        private Integer type;
        private String name;
    }
}
