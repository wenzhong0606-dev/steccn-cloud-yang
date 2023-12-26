package com.steccn.wms.dto;


import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BatchChargeAgainstDTO {
    /**
     *关联单号
     */
    private String code;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private  LocalDateTime endTime;
}
