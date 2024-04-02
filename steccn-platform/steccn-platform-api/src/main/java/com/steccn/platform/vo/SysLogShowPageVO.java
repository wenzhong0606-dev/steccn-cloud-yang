package com.steccn.platform.vo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author yequan
 * @desc 用户访问日志
 */
@Data
public class SysLogShowPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 日志类型（0-正常 1-错误）
     */
    private Integer type;

    /**
     * 日志标题
     */
    private String title;

    /**
     * ip地址
     */
    private String remoteAddr;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 请求uri
     */
    private String requestUri;

    /**
     * 请求方式
     */
    private String method;


    /**
     * 方法执行时间
     */
    private Long time;

    /**
     * 异常信息
     */
    private String exception;

    /**
     * 服务类型
     */
    private String serviceId;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
