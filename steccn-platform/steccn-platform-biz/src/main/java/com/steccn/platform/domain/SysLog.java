package com.steccn.platform.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author chenhuan
 * @date 2022/10/20
 * @TableName sys_log
 */
@Data
@TableName(value = "sys_log")
public class SysLog implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
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
     * 提交数据
     */
    private Object params;

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

    /**
     * 是否删除标识：0否 1是
     */
    private Integer delFlag;

    private static final long serialVersionUID = 1L;

}