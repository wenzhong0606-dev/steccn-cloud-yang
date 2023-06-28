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
     * 检验项名
     */
    @NotNull(message = "名称不能为空")
    private String name;

    /**
     * 检验项编码
     */
    private String code;

    /**
     * 产线名字
     */
    @NotNull(message = "产线不能为空")
    private String productLine;

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
