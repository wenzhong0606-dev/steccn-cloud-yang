package com.steccn.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.platform.domain.SysMsgUser;
import com.steccn.platform.service.SysMsgUserService;
import com.steccn.platform.mapper.SysMsgUserMapper;
import org.springframework.stereotype.Service;

/**
* @author yequan
* @description 针对表【sys_msg_user(用户接收消息表)】的数据库操作Service实现
* @createDate 2023-02-16 15:21:15
*/
@Service
public class SysMsgUserServiceImpl extends ServiceImpl<SysMsgUserMapper, SysMsgUser>
    implements SysMsgUserService{

}




