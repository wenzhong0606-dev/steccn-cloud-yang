
package com.steccn.common.aspect;

import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.authserver.util.SteccnUser;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.constant.LogTypeEnum;
import com.steccn.common.event.SysLogEvent;
import com.steccn.platform.dto.SysLogDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 操作日志使用spring event异步入库
 *
 * @author L.cm
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class SysLogAspect {

    private final ApplicationEventPublisher publisher;

    @SneakyThrows
    @Around("@annotation(apiOperation)")
    public Object around(ProceedingJoinPoint point, ApiOperation apiOperation) {
        String strClassName = point.getTarget().getClass().getName();
        String strMethodName = point.getSignature().getName();

        log.debug("[类名]:{},[方法]:{}", strClassName, strMethodName);

        SysLogDTO logDTO = getSysLog(point.getArgs());
        logDTO.setTitle(apiOperation.value());
        // 发送异步日志事件
        Long startTime = System.currentTimeMillis();
        Object obj;
        try {
            obj = point.proceed();
        } catch (Exception e) {
            logDTO.setType(LogTypeEnum.ERROR.getType());
            logDTO.setException(e.getMessage());
            throw e;
        } finally {
            Long endTime = System.currentTimeMillis();
            logDTO.setTime(endTime - startTime);
            publisher.publishEvent(new SysLogEvent(logDTO));
        }
        return obj;
    }

    /**
     * 获取日志对象
     * @return
     */
    private SysLogDTO getSysLog(Object[] args) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects
                .requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        SysLogDTO sysLog = new SysLogDTO();
        SteccnUser user = SecurityUtils.getUser();
        //白名单接口 是匿名类
        if (Objects.nonNull(user)) {
            sysLog.setCreateName(user.getName());
            sysLog.setCreateBy(user.getUserId());
        } else {
            sysLog.setCreateName("feign");
            sysLog.setCreateBy(0L);
        }
        sysLog.setCreateTime(LocalDateTime.now());
        sysLog.setType(LogTypeEnum.NORMAL.getType());
        sysLog.setRemoteAddr(ServletUtil.getClientIP(request));
        sysLog.setMethod(request.getMethod());
        sysLog.setUserAgent(request.getHeader("user-agent"));
        sysLog.setParams(JSONUtil.toJsonStr(args));
        //确定服务
        String requestUri = sysLog.getRequestUri();
        try {
            sysLog.setRequestUri(URLUtil.getPath(request.getRequestURI()));
            String[] split = requestUri.split("/");
            if (split[1].equals("feign")) {
                sysLog.setServiceId(split[2]);
            } else {
                sysLog.setServiceId(split[1]);
            }
        } catch (Exception e) {
            sysLog.setServiceId("none");
        }
        return sysLog;
    }
}
