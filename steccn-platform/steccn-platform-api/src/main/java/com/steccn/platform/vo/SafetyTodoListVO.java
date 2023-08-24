package com.steccn.platform.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * 安环待办list
 *
 * @author AndrewLuo
 * @date 2023/08/24
 */
@Data
public class SafetyTodoListVO implements Serializable {
    private String name;
    private Integer countNum;
    private String remindCode;
    private String page;
    private SafetyTodoListRouterObjVO routerObj;
    private SafetyTokenVO userInfo;
}
