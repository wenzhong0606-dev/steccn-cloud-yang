package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.common.constant.ResultCode;
import com.steccn.platform.domain.ProcessBusiLink;
import com.steccn.platform.dto.ProcessBusiLinkDTO;
import com.steccn.platform.service.ProcessBusiLinkService;
import com.steccn.platform.mapper.ProcessBusiLinkMapper;
import org.springframework.stereotype.Service;

/**
* @author W10
* @description 针对表【process_busi_link(流程业务关联)】的数据库操作Service实现
* @createDate 2023-04-03 14:19:42
*/
@Service
public class ProcessBusiLinkServiceImpl extends ServiceImpl<ProcessBusiLinkMapper, ProcessBusiLink>
    implements ProcessBusiLinkService{

    @Override
    public Boolean add(ProcessBusiLinkDTO processBusiLinkDTO) {
        return save(BeanUtil.copyProperties(processBusiLinkDTO,ProcessBusiLink.class));
    }

    @Override
    public ProcessBusiLinkDTO get(Integer instanceId) {
        ProcessBusiLink one = getOne(new LambdaQueryWrapper<ProcessBusiLink>().eq(ProcessBusiLink::getInstanceId, instanceId));
        return BeanUtil.copyProperties(one,ProcessBusiLinkDTO.class);
    }

    @Override
    public ProcessBusiLinkDTO getByBusiId(Long busiId) {
        ProcessBusiLink one = getOne(new LambdaQueryWrapper<ProcessBusiLink>().eq(ProcessBusiLink::getBusiId, busiId));
        return BeanUtil.copyProperties(one,ProcessBusiLinkDTO.class);
    }

    @Override
    public ProcessBusiLinkDTO getByTypeAndBusiId(Long busiId, Integer busiType) {
        ProcessBusiLink one = getOne(new LambdaQueryWrapper<ProcessBusiLink>().eq(ProcessBusiLink::getBusiType, busiType).eq(ProcessBusiLink::getBusiId, busiId));
        return BeanUtil.copyProperties(one,ProcessBusiLinkDTO.class);
    }

}




