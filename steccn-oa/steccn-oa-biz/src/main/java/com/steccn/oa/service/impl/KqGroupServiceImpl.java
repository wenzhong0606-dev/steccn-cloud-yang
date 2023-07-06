package com.steccn.oa.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.domain.*;
import com.steccn.oa.mapper.*;
import com.steccn.oa.service.KqGroupService;
import com.steccn.oa.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    private final KqFixedschedulceMapper kqFixedschedulceMapper;

    private final KqShiftscheduleMapper kqShiftscheduleMapper;

    @Override
    public List<OaKqGroupDetailVO> getKqGroupDetails() {
        List<OaKqGroupDetailVO> results = Lists.newArrayList();
        //1、获取所有数据
        //考勤组
        List<KqGroup> groupList = list(new LambdaQueryWrapper<KqGroup>()
                .select(KqGroup::getId, KqGroup::getGroupname, KqGroup::getKqtype,KqGroup::getWeekday)
                .isNull(KqGroup::getIsdelete));
        //班次信息
        List<KqShiftmanagement> shiftManagementList = kqShiftmanagementMapper.selectList(new LambdaQueryWrapper<KqShiftmanagement>()
                .select(KqShiftmanagement::getId, KqShiftmanagement::getSerial)
                .isNull(KqShiftmanagement::getIsdelete));
        //班次详情
        List<KqShiftonoffworksections> shiftManagementDetailList = kqShiftonoffworksectionsMapper.selectList(new LambdaQueryWrapper<KqShiftonoffworksections>()
                .select(KqShiftonoffworksections::getSerialid,KqShiftonoffworksections::getAcross,KqShiftonoffworksections::getTimes,KqShiftonoffworksections::getRecord,KqShiftonoffworksections::getOnoffworktype)
                .isNull(KqShiftonoffworksections::getIsdelete));
        //周期
        List<KqGroupShiftcycle> shiftcycleList = kqGroupShiftcycleMapper.selectList(new LambdaQueryWrapper<>());
        //固定班制
        List<KqFixedschedulce> fixedSchedulceList = kqFixedschedulceMapper.selectList(new LambdaQueryWrapper<KqFixedschedulce>()
                .isNull(KqFixedschedulce::getIsdelete));
        //找到最先开始
        List<KqShiftschedule> shiftscheduleList = kqShiftscheduleMapper.selectList(new QueryWrapper<KqShiftschedule>()
                .select("Min(kqdate) AS kqdate","groupid")
                .ne("isdelete",1)
                .groupBy("groupid"));
        //组成每个班次的map
        //组成数据结果
        groupList.forEach(tmp->{
            OaKqGroupDetailVO result = new OaKqGroupDetailVO();
            result.setGroupId(tmp.getId());
            result.setGroupName(tmp.getGroupname());
            result.setKqType(tmp.getKqtype());
            //只有周期制有开始时间
            if(tmp.getKqtype().equals(OaConstant.NUM_2)){
                KqShiftschedule kqShiftschedule = shiftscheduleList.stream().filter(v -> v.getGroupid().equals(tmp.getId())).findFirst().orElse(null);
                result.setStartTime(LocalDate.parse(kqShiftschedule.getKqdate()));
            }
            //固定班制没有周期找fixed表
            if(tmp.getKqtype().equals(OaConstant.NUM_1)){
                List<KqFixedschedulce> fixCollect = fixedSchedulceList.stream().filter(v -> v.getGroupid().equals(tmp.getId())).collect(Collectors.toList());
                if(CollUtil.isNotEmpty(fixCollect)){
                    OaKqGroupShiftCycleVO shiftCycle = new OaKqGroupShiftCycleVO();
                    List<OaKqGroupShiftCycleDetail> cycleDetails = Lists.newArrayList();
                    fixCollect.forEach(fix->{
                        OaKqGroupShiftCycleDetail cycleDetail = new OaKqGroupShiftCycleDetail();
                        int day = fix.getWeekday()+1;
                        cycleDetail.setDay("星期"+day);
                        //找班次 null说明休息
                        if(fix.getSerialid() == null || fix.getSerialid().equals(-1)){
                            cycleDetail.setShiftManagement(null);
                        }else {
                            cycleDetail.setShiftManagement(findShiftManage(shiftManagementList,shiftManagementDetailList,fix.getSerialid()));
                        }
                        cycleDetails.add(cycleDetail);
                    });
                    shiftCycle.setCycleDetails(cycleDetails);
                    result.setShiftCycle(shiftCycle);
                }
            }
            //排班制有周期
            if(tmp.getKqtype().equals(OaConstant.NUM_2)){
                KqGroupShiftcycle kqGroupShiftcycle = shiftcycleList.stream().filter(v -> v.getGroupid().equals(tmp.getId())).findFirst().orElse(null);
                if(kqGroupShiftcycle!=null){
                    OaKqGroupShiftCycleVO shiftCycle = new OaKqGroupShiftCycleVO();
                    shiftCycle.setCycleId(kqGroupShiftcycle.getId());
                    shiftCycle.setCycleName(kqGroupShiftcycle.getShiftcyclename());
                    shiftCycle.setCycleDay(kqGroupShiftcycle.getShiftcycleday());
                    List<String> shiftManageIds = Arrays.asList(kqGroupShiftcycle.getShiftcycleserialids().split(","));
                    List<OaKqGroupShiftCycleDetail> cycleDetails = Lists.newArrayList();
                    for(int i = 0; i <shiftManageIds.size(); i++){
                        String shiftId = shiftManageIds.get(i);
                        OaKqGroupShiftCycleDetail cycleDetail = new OaKqGroupShiftCycleDetail();
                        int day = i+1;
                        cycleDetail.setDay("第"+day+"天");
                        //找班次,-1说明休息
                        if(shiftId.equals("-1")){
                            cycleDetail.setShiftManagement(null);
                        }else {
                            cycleDetail.setShiftManagement(findShiftManage(shiftManagementList,shiftManagementDetailList,Integer.parseInt(shiftId)));
                        }
                        cycleDetails.add(cycleDetail);
                    }
                    shiftCycle.setCycleDetails(cycleDetails);
                    result.setShiftCycle(shiftCycle);
                }
            }
            //弹性工作则返回打卡时间
            if(tmp.getKqtype().equals(OaConstant.NUM_3)){
                if(tmp.getWeekday()!=null){
                    List<String> weekdays = Arrays.asList(tmp.getWeekday().split(","));
                    List<String> weekdayResult = Lists.newArrayList();
                    weekdays.forEach(day->{
                        Integer dayTmp = Integer.parseInt(day) + 1;
                        weekdayResult.add("星期"+dayTmp);
                    });
                    result.setFlexible(weekdayResult);
                }
            }
            results.add(result);
        });
        return results;
    }

    /**
     * 组班次数据
     * @return
     */
    private OaKqGroupShiftManageVO findShiftManage(List<KqShiftmanagement> shiftManagementList,List<KqShiftonoffworksections> shiftManagementDetailList,Integer shiftId){
        OaKqGroupShiftManageVO shiftManageVO = new OaKqGroupShiftManageVO();
        KqShiftmanagement kqShiftmanagement = shiftManagementList.stream().filter(v -> v.getId().equals(shiftId)).findFirst().orElse(null);
        shiftManageVO.setId(kqShiftmanagement.getId());
        shiftManageVO.setName(kqShiftmanagement.getSerial());
        //班次详情
        List<OaKqGroupShiftOnOffVO> timeDetail = Lists.newArrayList();
        List<KqShiftonoffworksections> kqShiftonoffworksections = shiftManagementDetailList.stream().filter(v -> v.getSerialid().equals(kqShiftmanagement.getId())).collect(Collectors.toList());
        kqShiftonoffworksections.forEach(kqShiftonoffworksection->{
            OaKqGroupShiftOnOffVO oaKqGroupShiftOnOffVO = new OaKqGroupShiftOnOffVO();
            oaKqGroupShiftOnOffVO.setAcross(kqShiftonoffworksection.getAcross());
            oaKqGroupShiftOnOffVO.setTimes(kqShiftonoffworksection.getTimes());
            oaKqGroupShiftOnOffVO.setOnOffWorkType(kqShiftonoffworksection.getOnoffworktype());
            oaKqGroupShiftOnOffVO.setRecord(kqShiftonoffworksection.getRecord());
            timeDetail.add(oaKqGroupShiftOnOffVO);
        });
        shiftManageVO.setTimeDetail(timeDetail);
        return shiftManageVO;
    }
}




