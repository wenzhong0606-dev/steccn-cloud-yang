package com.steccn.pms.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 数采数据DTO
 *
 * @author AndrewLuo
 * @date 2024/01/10
 */
@Data
public class DataCollectDTO {
    @JSONField(format="yyyy.MM.dd HH:mm:ss.SSS")
    private LocalDateTime timestamp;
    private BigDecimal value;
}
