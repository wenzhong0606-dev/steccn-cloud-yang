package com.steccn.qms.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 任务状态修改DTO
 *
 * @author AndrewLuo
 * @date 2022/09/09
 */
@Data
public class QtCheckTaskStatusDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 任务id
     */
    @NotEmpty
    private Long id;

    /**
     * 状态
     */
    @NotEmpty
    private Integer status;
}
