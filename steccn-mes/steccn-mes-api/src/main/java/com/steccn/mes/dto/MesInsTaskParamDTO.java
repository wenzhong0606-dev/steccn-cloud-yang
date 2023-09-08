package com.steccn.mes.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 点检任务参数DTO
 *
 * @author AndrewLuo
 * @date 2023/09/08
 */
@Data
public class MesInsTaskParamDTO implements Serializable {
    private Integer statusNe;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
