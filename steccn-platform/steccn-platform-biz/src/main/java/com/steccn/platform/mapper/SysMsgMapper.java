package com.steccn.platform.mapper;

import com.steccn.platform.domain.SysMsg;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【sys_msg(消息表)】的数据库操作Mapper
* @createDate 2023-02-16 15:21:15
* @Entity com.steccn.platform.domain.SysMsg
*/
@Mapper
public interface SysMsgMapper extends BaseMapper<SysMsg> {

}




