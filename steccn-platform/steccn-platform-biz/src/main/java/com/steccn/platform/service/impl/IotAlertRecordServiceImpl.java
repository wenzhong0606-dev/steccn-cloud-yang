package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.platform.domain.IotAlert;
import com.steccn.platform.domain.IotAlertRecord;
import com.steccn.platform.domain.PlatformUser;
import com.steccn.platform.dto.IotAlertRecordDTO;
import com.steccn.platform.service.IotAlertRecordService;
import com.steccn.platform.mapper.IotAlertRecordMapper;
import com.steccn.platform.service.PlatformUserService;
import com.steccn.platform.vo.IotAlertRecordVO;
import com.steccn.platform.vo.IotAlertVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【iot_alert_record(告警记录表)】的数据库操作Service实现
* @createDate 2025-12-15 15:32:15
*/
@Service
@RequiredArgsConstructor
public class IotAlertRecordServiceImpl extends ServiceImpl<IotAlertRecordMapper, IotAlertRecord>
    implements IotAlertRecordService{
    private final PlatformUserService platformUserService;

    @Override
    public Page<IotAlertRecordVO> listInfo(IotAlertRecordDTO dto) {
        Page<IotAlertRecord> page = page(new Page<>(dto.getCurrent(), dto.getSize()));
        Page resPage = new Page<IotAlertRecordVO>(dto.getCurrent(), dto.getSize());
        List<IotAlertRecordVO> results = Lists.newArrayList();
        if(CollUtil.isNotEmpty(page.getRecords())){
            Set<Long> collect = page.getRecords().stream().flatMap(v->v.getReceiver().stream()).collect(Collectors.toSet());
            List<PlatformUser> platformUsers = platformUserService.listByIds(collect);
            Map<Long, PlatformUser> userMap = platformUsers.stream()
                    .collect(Collectors.toMap(PlatformUser::getId, u -> u));
            page.getRecords().forEach(tmp->{
                IotAlertRecordVO vo = new IotAlertRecordVO();
                BeanUtil.copyProperties(tmp,vo);
                List<String> userNames = Lists.newArrayList();
                tmp.getReceiver().forEach(v->{
                    userNames.add(userMap.get(v).getName());
                });
                vo.setReceiverName(userNames);
                results.add(vo);
            });

        }
        BeanUtils.copyProperties(page, resPage);
        resPage.setRecords(results);
        return resPage;
    }
}




