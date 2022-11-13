package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author chenhuan
 * @date 2022/11/12 14:02
 */
@Data
public class PassRateDateVo implements Serializable {

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;
}
