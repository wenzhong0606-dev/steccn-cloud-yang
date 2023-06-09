package com.steccn.platform.dto;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc sys_configuration表dto映射
 */
@Data
public class SysConfigurationDTO implements Serializable {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 类型
     */
    private String type;

    /**
     * 内容
     */
    private String content;

    /**
     * 关联内容
     */
    private String refContent;

    /**
     * 其余内容
     */
    private String otherContent;

    private static final long serialVersionUID = 1L;
}