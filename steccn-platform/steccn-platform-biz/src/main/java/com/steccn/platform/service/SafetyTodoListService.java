package com.steccn.platform.service;

import com.steccn.platform.vo.SafetyTodoListVO;

import java.util.List;

/**
 * 获取安环系统待办列表
 *
 * @author AndrewLuo
 * @date 2023/08/24
 */
public interface SafetyTodoListService {
    /**
     * 获取安环系统待办
     * @return
     */
    List<SafetyTodoListVO> getTodoList();
}
