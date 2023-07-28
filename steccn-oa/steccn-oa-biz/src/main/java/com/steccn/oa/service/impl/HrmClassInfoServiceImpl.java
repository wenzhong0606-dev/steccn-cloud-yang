package com.steccn.oa.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.dto.TupleDTO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.domain.*;
import com.steccn.oa.dto.OaRosterRecordDTO;
import com.steccn.oa.mapper.*;
import com.steccn.oa.service.HrmClassInfoService;
import com.steccn.oa.service.OaService;
import com.steccn.oa.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/07/26
 */
@Service
@RequiredArgsConstructor
public class HrmClassInfoServiceImpl implements HrmClassInfoService {
    private final HrmresourceMapper hrmresourceMapper;
    private final KqGroupmemberMapper kqGroupmemberMapper;
    private final KqGroupMapper kqGroupMapper;
    private final OaService oaService;
    private final KqShiftmanagementMapper kqShiftmanagementMapper;
    private final KqShiftonoffworksectionsMapper kqShiftonoffworksectionsMapper;

    @Override
    public OaClassVO getClassByUserId(TupleDTO<LocalDate, Integer> dto) {
        if(Objects.isNull(dto.getField1())||Objects.isNull(dto.getField2())){
            throw new BusinessException("必传值不能为空");
        }
        OaClassVO result = new OaClassVO();
        //先查询人相关id
        Hrmresource hrmresource = hrmresourceMapper.selectOne(new LambdaQueryWrapper<Hrmresource>()
                .select(Hrmresource::getLastname, Hrmresource::getJobtitle,Hrmresource::getWorkcode)
                .eq(Hrmresource::getId, dto.getField2()).last("LIMIT 1"));
        if(Objects.isNull(hrmresource)){
            throw new BusinessException("该人员不存在");
        }
        result.setUserId(dto.getField2());
        result.setUserName(hrmresource.getLastname());
        //班组信息
        KqGroupmember groupId = kqGroupmemberMapper.selectOne(new LambdaQueryWrapper<KqGroupmember>()
                .select(KqGroupmember::getGroupid)
                .isNull(KqGroupmember::getIsdelete)
                .eq(KqGroupmember::getTypevalue, dto.getField2())
                .last("LIMIT 1"));
        if(Objects.nonNull(groupId)){
            KqGroup kqGroup = kqGroupMapper.selectOne(new LambdaQueryWrapper<KqGroup>()
                    .select(KqGroup::getGroupname)
                    .eq(KqGroup::getId, groupId.getGroupid())
                    .last("LIMIT 1"));
            if(Objects.nonNull(kqGroup)){
                result.setClassGroup(kqGroup.getGroupname());
            }
        }
        if(Objects.nonNull(hrmresource.getJobtitle())){
            OaRosterRecordDTO recordDTO = new OaRosterRecordDTO();
            recordDTO.setStartdate(dto.getField1().toString());
            recordDTO.setEnddate(dto.getField2().toString());
            recordDTO.setJobtitle(hrmresource.getJobtitle().toString());
            List<OaRosterRecordVO> rosterRecord = oaService.getRosterRecord(recordDTO);
            OaRosterRecordVO oaRosterRecordVO = rosterRecord.stream().filter(v -> v.getWorkcode().equals(hrmresource.getWorkcode())).findFirst().orElse(null);
            if(Objects.nonNull(oaRosterRecordVO)){
                result.setClassOrder(oaRosterRecordVO.getSerial());
                //找出班次的开始结束时间
                //班次信息
                KqShiftmanagement shiftManagement = kqShiftmanagementMapper.selectOne(new LambdaQueryWrapper<KqShiftmanagement>()
                        .select(KqShiftmanagement::getId, KqShiftmanagement::getSerial)
                        .eq(KqShiftmanagement::getSerial,oaRosterRecordVO.getSerial())
                        .isNull(KqShiftmanagement::getIsdelete));
                //班次开始、结束时间
                //班次详情
                if(Objects.nonNull(shiftManagement)){
                    List<KqShiftonoffworksections> shiftManagementDetailList = kqShiftonoffworksectionsMapper.selectList(new LambdaQueryWrapper<KqShiftonoffworksections>()
                            .select(KqShiftonoffworksections::getSerialid,KqShiftonoffworksections::getAcross,KqShiftonoffworksections::getTimes,KqShiftonoffworksections::getRecord,KqShiftonoffworksections::getOnoffworktype)
                            .eq(KqShiftonoffworksections::getSerialid,shiftManagement.getId())
                            .isNull(KqShiftonoffworksections::getIsdelete));
                    if(CollUtil.isNotEmpty(shiftManagementDetailList)){
                        OaKqGroupTimeRangeVO timeRange = findShiftManage(shiftManagement, shiftManagementDetailList).getTimeRange();
                        result.setStartTime(dto.getField1().atTime(timeRange.getStartTime()).withSecond(0));
                        if(timeRange.getIsAcross().equals(OaConstant.NUM_1)){
                            result.setEndTime(dto.getField1().plusDays(1).atTime(timeRange.getEndTime()).withSecond(0));
                        }else {
                            result.setEndTime(dto.getField1().atTime(timeRange.getEndTime()).withSecond(0));
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * 组班次数据
     * @return
     */
    private OaKqGroupShiftManageVO findShiftManage(KqShiftmanagement kqShiftmanagement, List<KqShiftonoffworksections> kqShiftonoffworksections){
        OaKqGroupShiftManageVO shiftManageVO = new OaKqGroupShiftManageVO();
        shiftManageVO.setId(kqShiftmanagement.getId());
        shiftManageVO.setName(kqShiftmanagement.getSerial());
        //班次详情
        List<OaKqGroupShiftOnOffVO> timeDetail = Lists.newArrayList();
        kqShiftonoffworksections.forEach(kqShiftonoffworksection->{
            OaKqGroupShiftOnOffVO oaKqGroupShiftOnOffVO = new OaKqGroupShiftOnOffVO();
            oaKqGroupShiftOnOffVO.setAcross(kqShiftonoffworksection.getAcross());
            oaKqGroupShiftOnOffVO.setTimes(kqShiftonoffworksection.getTimes());
            oaKqGroupShiftOnOffVO.setOnOffWorkType(kqShiftonoffworksection.getOnoffworktype());
            oaKqGroupShiftOnOffVO.setRecord(kqShiftonoffworksection.getRecord());
            timeDetail.add(oaKqGroupShiftOnOffVO);
        });
        if(CollUtil.isNotEmpty(kqShiftonoffworksections)){
            OaKqGroupTimeRangeVO timeRange = new OaKqGroupTimeRangeVO();
            //开始
            List<KqShiftonoffworksections> start = kqShiftonoffworksections.stream().filter(v -> v.getOnoffworktype().equals("start")).collect(Collectors.toList());
            LocalTime startTime = LocalTime.now();
            for(int i = 0; i < start.size(); i++){
                if(i==0){
                    startTime = LocalTime.parse(start.get(i).getTimes());
                    continue;
                }
                LocalTime tmpTime = LocalTime.parse(start.get(i).getTimes());
                if(tmpTime.isBefore(startTime)){
                    startTime = tmpTime;
                }
            }
            timeRange.setStartTime(startTime);
            //结束
            List<KqShiftonoffworksections> end = kqShiftonoffworksections.stream().filter(v -> v.getOnoffworktype().equals("end")).collect(Collectors.toList());
            LocalTime endTime = LocalTime.now();
            Integer across = 0;
            for(int i = 0; i < start.size(); i++){
                if(i==0){
                    endTime = LocalTime.parse(end.get(i).getTimes());
                    across = Integer.parseInt(end.get(i).getAcross());
                    continue;
                }
                LocalTime tmpTime = LocalTime.parse(end.get(i).getTimes());
                int tmpAcross = Integer.parseInt(end.get(i).getAcross());
                //先判断是否跨天
                if(tmpAcross>across){
                    across = tmpAcross;
                    endTime = tmpTime;
                }else if(tmpTime.isAfter(endTime)){
                    endTime = tmpTime;
                }
            }
            timeRange.setEndTime(endTime);
            timeRange.setIsAcross(across);
            shiftManageVO.setTimeRange(timeRange);
        }
        shiftManageVO.setTimeDetail(timeDetail);
        return shiftManageVO;
    }
}
