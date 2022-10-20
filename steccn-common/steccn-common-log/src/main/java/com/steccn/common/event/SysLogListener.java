package com.steccn.common.event;

import com.steccn.common.constant.LogServiceEnum;
import com.steccn.common.properties.LogProperties;
import com.steccn.platform.dto.SysLogDTO;
import com.steccn.platform.feign.RemoteLogFeignApi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

/**
 * @author yequan
 * @desc 异步监听日志事件
 */
@Slf4j
@AllArgsConstructor
public class SysLogListener {

	private final LogProperties logProperties;

	private final RemoteLogFeignApi remoteLogFeignApi;

	@Order
	@EventListener(SysLogEvent.class)
	public void saveSysLog(SysLogEvent event) {
		SysLogDTO sysLog = event.getSysLog();
		if(logProperties.getEnable()) {
			if (logProperties.getLogType().equals(LogServiceEnum.DB)) {
		       //todo 异步 -> 远程过程调用接口
				remoteLogFeignApi.add(sysLog);
				log.debug("saveSysLog -> {}",sysLog);
			} else if (logProperties.getLogType().equals(LogServiceEnum.LOGGER)) {
				log.info("request log : {} ", event.getSysLog());
			}
		}
	}
}
