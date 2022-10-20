package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.domain.SysLog;
import com.steccn.platform.dto.SysLogDTO;

/**
 * @author chenhuan
 * @date 2022/10/20
*/
public interface SysLogService extends IService<SysLog> {

    Boolean add(SysLogDTO sysLogDTO);
}
