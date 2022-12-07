package com.steccn.pms.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author chenhuan
 * @date 2022/12/6 19:15
 */
@Data
public class WorkOrderVO {

    private String materialCode;

    private LocalDateTime endTime;
}
