package com.steccn.qms.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class QtCheckTaskCopyVO {
    /**
     * 任务id
     */
   private Integer taskId;

    /**
     * 取样时间
     */
   private LocalDate samplingTime;
}
