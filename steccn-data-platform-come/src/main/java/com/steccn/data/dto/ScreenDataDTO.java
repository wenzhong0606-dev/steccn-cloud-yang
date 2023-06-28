package com.steccn.data.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 屏幕数据DTO
 *
 * @author AndrewLuo
 * @date 2023/06/28
 */
@Data
public class ScreenDataDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 检验项编码
     */
    @NotNull(message = "编码不能为空")
    private String code;

    /**
     * 检验项名字
     */
    @NotNull(message = "值不能为空")
    private BigDecimal value;

    /**
     * 记录时间
     */
    @NotNull(message = "记录时间不能为空")
    private LocalDateTime recordTime;
}
