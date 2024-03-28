package com.steccn.platform.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 用户访问日志
 */
@Data
public class SysLogShowPageDTO extends BasePageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户主键
     */
    private Long userId;

    /**
     * 开始时间
     */
    private LocalDate start;

    /**
     * 结束时间
     */
    private LocalDate end;
}
