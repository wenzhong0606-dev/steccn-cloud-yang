package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.ems.domain.EpTaskExecuteRetry;
import com.steccn.ems.service.EpTaskExecuteRetryService;
import com.steccn.ems.mapper.EpTaskExecuteRetryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yequan
 * @description 针对表【ep_task_execute_retry】的数据库操作Service实现
 * @createDate 2023-07-13 14:32:26
 */
@Service
public class EpTaskExecuteRetryServiceImpl extends ServiceImpl<EpTaskExecuteRetryMapper, EpTaskExecuteRetry>
        implements EpTaskExecuteRetryService {
}




