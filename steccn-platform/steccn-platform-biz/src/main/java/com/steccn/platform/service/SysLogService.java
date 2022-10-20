package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.domain.SysLog;
import com.steccn.platform.dto.SysLogDTO;

/**
 * @author chenhuan
 * @date 2022/10/20
*/
public interface SysLogService extends IService<SysLog> {

    /**
     * 新增用户操作日志
     * @param sysLogDTO 用户操作信息
     * @return 是否成功
     */
    Boolean add(SysLogDTO sysLogDTO);
}
