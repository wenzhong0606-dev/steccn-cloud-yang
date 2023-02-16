package com.steccn.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.platform.domain.SysMsg;
import com.steccn.platform.service.SysMsgService;
import com.steccn.platform.mapper.SysMsgMapper;
import org.springframework.stereotype.Service;

/**
* @author yequan
* @description 针对表【sys_msg(消息表)】的数据库操作Service实现
* @createDate 2023-02-16 15:21:15
*/
@Service
public class SysMsgServiceImpl extends ServiceImpl<SysMsgMapper, SysMsg>
    implements SysMsgService{

}




