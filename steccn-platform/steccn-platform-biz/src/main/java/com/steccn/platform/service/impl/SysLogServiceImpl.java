package com.steccn.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.platform.domain.SysLog;
import com.steccn.platform.dto.SysLogDTO;
import com.steccn.platform.service.SysLogService;
import com.steccn.platform.mapper.SysLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author chenhuan
 * @date 2022/10/20
*/
@RequiredArgsConstructor
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog>
implements SysLogService{

    @Override
    public Boolean add(SysLogDTO sysLogDTO) {
        SysLog sysLog = new SysLog();
        BeanUtils.copyProperties(sysLogDTO,sysLog);
        return save(sysLog);
    }
}
