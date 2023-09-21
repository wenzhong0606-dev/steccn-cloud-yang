package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 校验在线检
 *
 * @author AndrewLuo
 * @date 2023/09/21
 */
@Data
public class QtCheckOnlineTaskVO implements Serializable {
    /**
     * 是否过验证
     */
    private Boolean isChecked;
    /**
     * 缺陷记录开始时间
     */
    private LocalDateTime defectStartTime;

    /**
     * 缺陷记录结束时间
     */
    private LocalDateTime defectEndTime;

    /**
     * 提示消息
     */
    private String msg;
}
