package com.steccn.platform.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.MpConstant;
import com.steccn.platform.domain.SysMsg;
import com.steccn.platform.domain.SysMsgUser;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.service.SysMsgService;
import com.steccn.platform.mapper.SysMsgMapper;
import com.steccn.platform.service.SysMsgUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yequan
 * @description 针对表【sys_msg(消息表)】的数据库操作Service实现
 * @createDate 2023-02-16 15:21:15
 */
@Service
@RequiredArgsConstructor
public class SysMsgServiceImpl extends ServiceImpl<SysMsgMapper, SysMsg>
        implements SysMsgService {

    private final SysMsgUserService sysMsgUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean sendMsg(SysMsgSendDTO sysMsgDTO) {
        //1.保存消息主体
        SysMsg msg = new SysMsg();
        BeanUtils.copyProperties(sysMsgDTO, msg);
        msg.setId(IdUtil.getSnowflake().nextId());
        //2.保存消息接收人
        List<SysMsgUser> msgUserList = new ArrayList<>(sysMsgDTO.getUserList().size());
        sysMsgDTO.getUserList().forEach(ele -> {
            SysMsgUser sysMsgUser = new SysMsgUser();
            sysMsgUser.setMsgId(msg.getId());
            sysMsgUser.setUserId(ele);
            sysMsgUser.setReadFlag(MpConstant.NO);
            msgUserList.add(sysMsgUser);
        });
        sysMsgUserService.saveBatch(msgUserList);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean read(List<Long> msgIdList) {
        //将所有的消息都更新成已读，赋值已读时间
        LambdaUpdateWrapper<SysMsgUser> updateWrapper = new LambdaUpdateWrapper<SysMsgUser>()
                .set(SysMsgUser::getReadFlag, MpConstant.YES)
                .set(SysMsgUser::getReadTime, LocalDateTime.now())
                .in(SysMsgUser::getMsgId, msgIdList)
                .eq(SysMsgUser::getUserId, SecurityUtils.getUser().getUserId());
        return sysMsgUserService.update(updateWrapper);
    }
}