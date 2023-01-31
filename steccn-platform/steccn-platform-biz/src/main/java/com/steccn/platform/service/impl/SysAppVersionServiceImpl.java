package com.steccn.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.platform.domain.SysAppVersion;
import com.steccn.platform.service.SysAppVersionService;
import com.steccn.platform.mapper.SysAppVersionMapper;
import org.springframework.stereotype.Service;

/**
* @author yequan
* @description 针对表【sys_app_version(app的版本控制表)】的数据库操作Service实现
* @createDate 2023-01-31 14:02:33
*/
@Service
public class SysAppVersionServiceImpl extends ServiceImpl<SysAppVersionMapper, SysAppVersion>
    implements SysAppVersionService{

}




