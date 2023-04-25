package com.steccn.data.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 接收设备启停信号DTO
 *
 * @author AndrewLuo
 * @date 2023/04/25
 */
@Data
public class MesEqpSignalDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 停止/运行 0/1
     */
    private Integer value;

    /**
     * 设备编码
     */
    private String code;

    /**
     * 时间
     */
    private LocalDateTime timestamp;
}
