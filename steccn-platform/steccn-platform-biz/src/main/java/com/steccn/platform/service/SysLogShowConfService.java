package com.steccn.platform.service;

import com.steccn.platform.domain.SysLogShowConf;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Set;

/**
* @author yequan
* @description 针对表【sys_log_show_conf】的数据库操作Service
* @createDate 2024-03-27 15:39:38
*/
public interface SysLogShowConfService extends IService<SysLogShowConf> {

    List<SysLogShowConf> requestUrlList();
}
