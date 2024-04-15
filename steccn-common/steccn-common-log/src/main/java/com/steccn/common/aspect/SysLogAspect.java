
package com.steccn.common.aspect;

import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.json.JSONUtil;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.authserver.util.SteccnUser;
import com.steccn.common.constant.LogTypeEnum;
import com.steccn.common.event.SysLogEvent;
import com.steccn.platform.dto.SysLogDTO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.context.request.RequestAttributes;
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
        if (Objects.nonNull(logDTO)) {
            logDTO.setTitle(apiOperation.value());
        }
        //如果是feign请求，直接返回null
        if (point.toString().toUpperCase().indexOf("FEIGN") != -1) {
            logDTO = null;
        }

        // 发送异步日志事件
        Long startTime = System.currentTimeMillis();
        Object obj;
        try {
            obj = point.proceed();
        } catch (Exception e) {
            if(Objects.nonNull(logDTO)){
                logDTO.setType(LogTypeEnum.ERROR.getType());
                logDTO.setException(e.getMessage());
            }
            throw e;
        } finally {
            Long endTime = System.currentTimeMillis();
            if (Objects.nonNull(logDTO)) {
                logDTO.setTime(endTime - startTime);
                publisher.publishEvent(new SysLogEvent(logDTO));
            }
        }
        return obj;
    }

    /**
     * 获取日志对象
     *
     * @return
     */
    private SysLogDTO getSysLog(Object[] args) {
        //定时任务
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(attributes)) {
            return null;
        }
        HttpServletRequest request = ((ServletRequestAttributes) Objects
                .requireNonNull(attributes)).getRequest();
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
        String requestUri = request.getRequestURI();
        try {
            sysLog.setRequestUri(URLUtil.getPath(request.getRequestURI()));
            String[] split = requestUri.split("/");
            //白名单接口，不保存
            if (split[1].equals("feign")) {
                return null;
            } else {
                sysLog.setServiceId(split[1]);
            }
        } catch (Exception e) {
            sysLog.setServiceId("none");
        }
        return sysLog;
    }
}
