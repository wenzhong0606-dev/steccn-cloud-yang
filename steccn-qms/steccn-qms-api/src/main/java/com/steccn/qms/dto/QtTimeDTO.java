package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 时间DTO
 *
 * @author AndrewLuo
 * @date 2022/11/15
 */
@Data
public class QtTimeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDateTime startTime;
}
