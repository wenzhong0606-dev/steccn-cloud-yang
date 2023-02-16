package com.steccn.platform.mapper;

import com.steccn.platform.domain.SysMsgUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【sys_msg_user(用户接收消息表)】的数据库操作Mapper
* @createDate 2023-02-16 15:21:15
* @Entity com.steccn.platform.domain.SysMsgUser
*/
@Mapper
public interface SysMsgUserMapper extends BaseMapper<SysMsgUser> {

}




