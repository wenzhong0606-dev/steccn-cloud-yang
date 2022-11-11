package com.steccn.pms.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/9 16:49
 */
@Data
public class ProducePassRateDTO {

    private LocalDateTime beginDate;

    private LocalDateTime endDate;

}
