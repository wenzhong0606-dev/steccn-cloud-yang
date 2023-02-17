package com.steccn.platform.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.platform.domain.SysMsg;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.SysMsgSendDTO;

import java.util.List;

/**
 * @author yequan
 * @description 针对表【sys_msg(消息表)】的数据库操作Service
 * @createDate 2023-02-16 15:21:15
 */
public interface SysMsgService extends IService<SysMsg> {

    /**
     * 发送消息
     *
     * @param sysMsgDTO 消息实体
     * @return 是否成功
     */
    Boolean sendMsg(SysMsgSendDTO sysMsgDTO);

    /**
     * 读取消息
     *
     * @param msgIdList 消息id集合
     * @return 是否成功
     */
    Boolean read(List<Long> msgIdList);
}
