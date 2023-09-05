package com.steccn.data.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 运行状态VO
 *
 * @author AndrewLuo
 * @date 2023/09/05
 */
@Data
public class OperationStateVO implements Serializable {
    /**
     * 状态
     */
    private Integer state;
    /**
     * 时间
     */
    private LocalDateTime planTime;
}
