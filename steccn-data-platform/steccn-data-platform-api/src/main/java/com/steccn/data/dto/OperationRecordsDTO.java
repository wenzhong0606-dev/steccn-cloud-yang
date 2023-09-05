package com.steccn.data.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 运行记录DTO
 *
 * @author AndrewLuo
 * @date 2023/09/05
 */
@Data
public class OperationRecordsDTO extends BasePageDTO implements Serializable {
    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    /**
     * 设备code
     */
    @NotEmpty(message = "设备编码不能为空")
    private String eqpCode;
}
