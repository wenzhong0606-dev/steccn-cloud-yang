package com.steccn.platform.scheduled;

import com.steccn.platform.service.SafetyTodoListService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 发送消息task
 *
 * @author AndrewLuo
 * @date 2024/01/26
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SafetySyncTask {
    private final SafetyTodoListService safetyTodoListService;

    @XxlJob("sendSafetyMsg")
    public void sendSafetyMsg() {
        XxlJobHelper.log("发送安环待办消息-任务开始");
        Boolean flag = safetyTodoListService.sendMsg();
        XxlJobHelper.log("发送安环待办消息，执行结果 -> {}", flag);
    }
}
