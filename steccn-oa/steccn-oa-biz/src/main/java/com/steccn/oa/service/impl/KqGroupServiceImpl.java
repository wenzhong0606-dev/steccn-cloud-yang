package com.steccn.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.oa.domain.KqGroup;
import com.steccn.oa.domain.KqGroupShiftcycle;
import com.steccn.oa.domain.KqShiftmanagement;
import com.steccn.oa.domain.KqShiftonoffworksections;
import com.steccn.oa.mapper.KqGroupShiftcycleMapper;
import com.steccn.oa.mapper.KqShiftmanagementMapper;
import com.steccn.oa.mapper.KqShiftonoffworksectionsMapper;
import com.steccn.oa.service.KqGroupService;
import com.steccn.oa.mapper.KqGroupMapper;
import com.steccn.oa.vo.OaKqGroupDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Admin
* @description 针对表【kq_group】的数据库操作Service实现
* @createDate 2023-04-04 11:18:13
*/
@Service
@RequiredArgsConstructor
public class KqGroupServiceImpl extends ServiceImpl<KqGroupMapper, KqGroup>
    implements KqGroupService{
    private final KqShiftmanagementMapper kqShiftmanagementMapper;

    private final KqShiftonoffworksectionsMapper kqShiftonoffworksectionsMapper;

    private final KqGroupShiftcycleMapper kqGroupShiftcycleMapper;

    @Override
    public List<OaKqGroupDetailVO> getKqGroupDetails() {
        List<OaKqGroupDetailVO> results = Lists.newArrayList();
        //1、获取所有数据
        //考勤组
        List<KqGroup> groupList = list(new LambdaQueryWrapper<KqGroup>()
                .select(KqGroup::getId, KqGroup::getGroupname, KqGroup::getKqtype)
                .isNull(KqGroup::getIsdelete));
        //班次信息
        List<KqShiftmanagement> shiftManagementList = kqShiftmanagementMapper.selectList(new LambdaQueryWrapper<KqShiftmanagement>()
                .select(KqShiftmanagement::getId, KqShiftmanagement::getSerial)
                .isNull(KqShiftmanagement::getIsdelete));
        //班次详情
        List<KqShiftonoffworksections> shiftManagementDetailList = kqShiftonoffworksectionsMapper.selectList(new LambdaQueryWrapper<KqShiftonoffworksections>()
                .select(KqShiftonoffworksections::getSerialid,KqShiftonoffworksections::getAcross,KqShiftonoffworksections::getTimes,KqShiftonoffworksections::getRecord)
                .isNull(KqShiftonoffworksections::getIsdelete));
        //周期
//        kqGroupShiftcycleMapper
        return null;
    }
}




