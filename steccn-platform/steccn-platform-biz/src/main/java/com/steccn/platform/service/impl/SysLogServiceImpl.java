package com.steccn.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.platform.domain.SysLog;
import com.steccn.platform.domain.SysLogShow;
import com.steccn.platform.domain.SysLogShowConf;
import com.steccn.platform.dto.SysLogDTO;
import com.steccn.platform.service.SysLogService;
import com.steccn.platform.mapper.SysLogMapper;
import com.steccn.platform.service.SysLogShowConfService;
import com.steccn.platform.service.SysLogShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * @author chenhuan
 * @date 2022/10/20
 */
@RequiredArgsConstructor
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog>
        implements SysLogService {

    private final SysLogShowConfService sysLogShowConfService;
    private final SysLogShowService sysLogShowService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(SysLogDTO sysLogDTO) {
        //判断如果是用户感兴趣的日志url则插入sys_log_show
        List<SysLogShowConf> requestUrlList = sysLogShowConfService.requestUrlList();
        long count = requestUrlList.stream().filter(ele -> ele.getMethod().equals(sysLogDTO.getMethod()) && sysLogDTO.getRequestUri().indexOf(ele.getRequestUrl()) != -1).count();
        if (count != 0) {
            SysLogShow sysLogShow = new SysLogShow();
            BeanUtils.copyProperties(sysLogDTO, sysLogShow);
            sysLogShowService.save(sysLogShow);
        }
        SysLog sysLog = new SysLog();
        BeanUtils.copyProperties(sysLogDTO, sysLog);
        return save(sysLog);
    }
}
