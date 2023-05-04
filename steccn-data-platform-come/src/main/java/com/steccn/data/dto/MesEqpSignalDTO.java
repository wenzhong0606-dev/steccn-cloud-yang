package com.steccn.data.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
    @Max(value = 1, message = "状态只能是 1是  0否")
    @Min(value = 0, message = "状态只能是 1是  0否")
    @NotNull(message = "状态值不能为空")
    private Integer value;

    /**
     * 设备编码
     */
    @NotEmpty(message = "设备id不能为空")
    private String code;

    /**
     * 时间
     */
    @NotNull(message = "时间戳不能为空")
    private LocalDateTime timestamp;
}
