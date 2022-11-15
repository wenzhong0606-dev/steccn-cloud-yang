package com.steccn.pms.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author chenhuan
 * @date 2022/11/15 15:35
 */
@Data
public class PassRateDateVo {

    private LocalDateTime endDateTime;

    private LocalDateTime startDateTime;
}
