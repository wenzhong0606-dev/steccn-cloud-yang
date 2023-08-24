package com.steccn.platform.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 安环待办列表跳转对象
 *
 * @author AndrewLuo
 * @date 2023/08/24
 */
@Data
public class SafetyTodoListRouterObjVO implements Serializable {
    private String remind_code;
    private String page;
    private String title;
}
