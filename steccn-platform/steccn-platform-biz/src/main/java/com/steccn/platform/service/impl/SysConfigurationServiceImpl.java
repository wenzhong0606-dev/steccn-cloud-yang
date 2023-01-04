package com.steccn.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.platform.domain.SysConfiguration;
import com.steccn.platform.service.SysConfigurationService;
import com.steccn.platform.mapper.SysConfigurationMapper;
import org.springframework.stereotype.Service;

/**
* @author yequan
* @description 针对表【sys_configuration】的数据库操作Service实现
* @createDate 2023-01-04 15:06:46
*/
@Service
public class SysConfigurationServiceImpl extends ServiceImpl<SysConfigurationMapper, SysConfiguration>
    implements SysConfigurationService{

}


