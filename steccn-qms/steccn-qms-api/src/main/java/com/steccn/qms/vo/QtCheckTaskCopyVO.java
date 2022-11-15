package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class QtCheckTaskCopyVO implements Serializable {
    /**
     * 任务id
     */
   private Long taskId;

    /**
     * 取样时间
     */
   private LocalDateTime samplingTime;
}
