package com.steccn.mes.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.StackGraphDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.StackGraphUtil;
import com.steccn.mes.domain.JsSysOffice;
import com.steccn.mes.domain.MesEqpInsTask;
import com.steccn.mes.dto.MesInsTaskCountDTO;
import com.steccn.mes.dto.MesInsTaskParamDTO;
import com.steccn.mes.mapper.JsSysOfficeMapper;
import com.steccn.mes.service.MesEqpInsTaskService;
import com.steccn.mes.mapper.MesEqpInsTaskMapper;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.qms.constant.QtManagerConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.http.util.Asserts;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【mes_eqp_ins_task(点检任务表)】的数据库操作Service实现
* @createDate 2023-09-08 14:56:45
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class MesEqpInsTaskServiceImpl extends ServiceImpl<MesEqpInsTaskMapper, MesEqpInsTask>
    implements MesEqpInsTaskService{
    private final MesEqpInsTaskMapper mesEqpInsTaskMapper;
    private final JsSysOfficeMapper jsSysOfficeMapper;
    private final RemoteOfficeFeign remoteOfficeFeign;

    @Override
    public QtLineChartBarDTO taskComplete(LocalDateTime startTime, LocalDateTime endTime,Integer type) {
        //获取数据
        MesInsTaskParamDTO dto = new MesInsTaskParamDTO();
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);
        dto.setStatusNe(OaConstant.NUM_1);
        //所有任务数据
        List<MesInsTaskCountDTO> allTask = mesEqpInsTaskMapper.findTaskCount(dto);
        Assert.notEmpty(allTask,"数据为空");
        Map<String, Long> allTaskMap = allTask.stream().filter(v -> v.getOfficeCode() != null)
                .collect(Collectors.toMap(
                        MesInsTaskCountDTO::getOfficeCode,
                        MesInsTaskCountDTO::getNum
                ));
        List<String> officeCodes = allTask.stream().filter(v -> v.getOfficeCode() != null)
                .map(MesInsTaskCountDTO::getOfficeCode).collect(Collectors.toList());
        dto.setStatusNe(null);
        dto.setStatus(OaConstant.NO);
        //完成任务数据
        List<MesInsTaskCountDTO> finishedTask = mesEqpInsTaskMapper.findTaskCount(dto);
        Map<String, Long> finishedTaskMap = finishedTask.stream().filter(v -> v.getOfficeCode() != null)
                .collect(Collectors.toMap(
                        MesInsTaskCountDTO::getOfficeCode,
                        MesInsTaskCountDTO::getNum
                ));
        //组成图表数据
        List<JsSysOffice> jsSysOffices = jsSysOfficeMapper.selectList(new LambdaQueryWrapper<JsSysOffice>()
                .select(JsSysOffice::getOfficeCode,JsSysOffice::getOfficeName,JsSysOffice::getParentCodes)
                .in(JsSysOffice::getOfficeCode, officeCodes).eq(JsSysOffice::getStatus, QtManagerConstant.NUM_0));
//        Collections.sort(jsSysOffices, Comparator.comparing(JsSysOffice::getOfficeName));
        //type = 0 车间粒度
        List<JsSysOffice> officeList = Lists.newArrayList();
        if(type.equals(OaConstant.NO)){
            officeList = jsSysOfficeMapper.selectList(new LambdaQueryWrapper<JsSysOffice>()
                    .select(JsSysOffice::getOfficeCode,JsSysOffice::getOfficeName)
                    .eq(JsSysOffice::getOfficeType,QtManagerConstant.NUM_8)
                    .eq(JsSysOffice::getStatus, QtManagerConstant.NUM_0)
                    .orderByAsc(JsSysOffice::getTreeSort));
        }else {
            //根据车间id获取车间
            Map<Integer, SysOfficeVO> integerSysOfficeVOMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(type)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysOfficeVO sysOfficeVO = integerSysOfficeVOMap.get(type);
            officeList = jsSysOfficeMapper.selectList(new LambdaQueryWrapper<JsSysOffice>()
                    .select(JsSysOffice::getOfficeCode,JsSysOffice::getOfficeName)
                    .like(JsSysOffice::getParentCodes,sysOfficeVO.getOfficeCode())
                    .eq(JsSysOffice::getStatus, QtManagerConstant.NUM_0)
                    .orderByAsc(JsSysOffice::getTreeSort));
            Assert.notEmpty(officeList,"该车间下没有机构");
            officeList = officeList.stream().filter(v->officeCodes.contains(v.getOfficeCode())).collect(Collectors.toList());
        }
        Assert.notEmpty(officeList,"数据为空");
        List<StackGraphDTO<String, BigDecimal>> dataList = Lists.newArrayList();
        officeList.forEach(ele->{
            String officeName = ele.getOfficeName();
            //聚合粒度下所有的数据
            List<JsSysOffice> officeTmp = Lists.newArrayList();
            if(type.equals(OaConstant.NO)){
                officeTmp = jsSysOffices.stream().filter(v -> v.getParentCodes().contains(ele.getOfficeCode())).collect(Collectors.toList());
            }else {
                officeTmp = jsSysOffices.stream().filter(v -> v.getOfficeCode().equals(ele.getOfficeCode())).collect(Collectors.toList());
            }
            if(CollUtil.isNotEmpty(officeTmp)){
                //加总产线下所有 1任务总数 2完成数
                Map<Integer,BigDecimal> countMap = Maps.newHashMap();
                officeTmp.forEach(tmp->{
                    //任务总数
                    BigDecimalUtil.accumulate(countMap,1,new BigDecimal(allTaskMap.get(tmp.getOfficeCode())));
                    //完成数
                    BigDecimalUtil.accumulate(countMap,2,new BigDecimal(finishedTaskMap.getOrDefault(tmp.getOfficeCode(),0L)));
                });
                //组数据
                StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
                stackGraphDTO.setXAxis(officeName);
                List<TupleDTO<String, BigDecimal>> datList = Lists.newArrayList();
                BigDecimal all = countMap.get(1);
                BigDecimal finished = countMap.get(2);
                datList.add(new TupleDTO<>("任务总数", all));
                datList.add(new TupleDTO<>("完成数", finished));
                datList.add(new TupleDTO<>("完成率", BigDecimalUtil.percent(finished,all,2)));
                stackGraphDTO.setData(datList);
                dataList.add(stackGraphDTO);
            }
        });
//        jsSysOffices.forEach(ele->{
//            String officeName = ele.getOfficeName();
//            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
//            stackGraphDTO.setXAxis(officeName);
//            List<TupleDTO<String, BigDecimal>> datList = Lists.newArrayList();
//            BigDecimal all = new BigDecimal(allTaskMap.get(ele.getOfficeCode()));
//            BigDecimal finished = new BigDecimal(finishedTaskMap.getOrDefault(ele.getOfficeCode(),0L));
//            datList.add(new TupleDTO<>("任务总数", all));
//            datList.add(new TupleDTO<>("完成数", finished));
//            datList.add(new TupleDTO<>("完成率", BigDecimalUtil.percent(finished,all,2)));
//            stackGraphDTO.setData(datList);
//            dataList.add(stackGraphDTO);
//        });
        return StackGraphUtil.convertData(dataList, Boolean.TRUE);
    }

    @Override
    public QtLineChartBarDTO itemComplete(LocalDateTime startTime, LocalDateTime endTime) {
        //获取数据
        MesInsTaskParamDTO dto = new MesInsTaskParamDTO();
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);
        //所有任务数据
        List<MesInsTaskCountDTO> allTask = mesEqpInsTaskMapper.findInsCount(dto);
        Assert.notEmpty(allTask,"数据为空");
        Map<String, Long> allTaskMap = allTask.stream().filter(v -> v.getOfficeCode() != null)
                .collect(Collectors.toMap(
                        MesInsTaskCountDTO::getOfficeCode,
                        MesInsTaskCountDTO::getNum
                ));
        List<String> officeCodes = allTask.stream().filter(v -> v.getOfficeCode() != null)
                .map(MesInsTaskCountDTO::getOfficeCode).collect(Collectors.toList());
        dto.setStatus(OaConstant.NUM_1);
        //合格数据
        List<MesInsTaskCountDTO> finishedTask = mesEqpInsTaskMapper.findInsCount(dto);
        Map<String, Long> finishedTaskMap = finishedTask.stream().filter(v -> v.getOfficeCode() != null)
                .collect(Collectors.toMap(
                        MesInsTaskCountDTO::getOfficeCode,
                        MesInsTaskCountDTO::getNum
                ));
        //组成图表数据
        List<JsSysOffice> jsSysOffices = jsSysOfficeMapper.selectList(new LambdaQueryWrapper<JsSysOffice>()
                .select(JsSysOffice::getOfficeCode,JsSysOffice::getOfficeName,JsSysOffice::getParentCodes)
                .in(JsSysOffice::getOfficeCode, officeCodes).eq(JsSysOffice::getStatus, QtManagerConstant.NUM_0));
//        Collections.sort(jsSysOffices, Comparator.comparing(JsSysOffice::getOfficeName));
        //车间粒度
        List<JsSysOffice> officeList = jsSysOfficeMapper.selectList(new LambdaQueryWrapper<JsSysOffice>()
                .select(JsSysOffice::getOfficeCode,JsSysOffice::getOfficeName)
                .eq(JsSysOffice::getOfficeType,QtManagerConstant.NUM_8)
                .eq(JsSysOffice::getStatus, QtManagerConstant.NUM_0)
                .orderByAsc(JsSysOffice::getTreeSort));
        List<StackGraphDTO<String, BigDecimal>> dataList = Lists.newArrayList();
        officeList.forEach(ele->{
            String officeName = ele.getOfficeName();
            List<JsSysOffice> officeTmp = jsSysOffices.stream().filter(v -> v.getParentCodes().contains(ele.getOfficeCode())).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(officeTmp)){
                //加总产线下所有 1任务总数 2完成数
                Map<Integer,BigDecimal> countMap = Maps.newHashMap();
                officeTmp.forEach(tmp->{
                    //任务总数
                    BigDecimalUtil.accumulate(countMap,1,new BigDecimal(allTaskMap.get(tmp.getOfficeCode())));
                    //完成数
                    BigDecimalUtil.accumulate(countMap,2,new BigDecimal(finishedTaskMap.getOrDefault(tmp.getOfficeCode(),0L)));
                });
                StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
                stackGraphDTO.setXAxis(officeName);
                List<TupleDTO<String, BigDecimal>> datList = Lists.newArrayList();
                BigDecimal all = countMap.get(1);
                BigDecimal finished = countMap.get(2);
                datList.add(new TupleDTO<>("点检项总数", all));
                datList.add(new TupleDTO<>("合格数", finished));
                datList.add(new TupleDTO<>("合格率", BigDecimalUtil.percent(finished,all,2)));
                stackGraphDTO.setData(datList);
                dataList.add(stackGraphDTO);
            }
        });

//        jsSysOffices.forEach(ele->{
//            String officeName = ele.getOfficeName();
//            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
//            stackGraphDTO.setXAxis(officeName);
//            List<TupleDTO<String, BigDecimal>> datList = Lists.newArrayList();
//            BigDecimal all = new BigDecimal(allTaskMap.get(ele.getOfficeCode()));
//            BigDecimal finished = new BigDecimal(finishedTaskMap.getOrDefault(ele.getOfficeCode(),0L));
//            datList.add(new TupleDTO<>("点检项总数", all));
//            datList.add(new TupleDTO<>("合格数", finished));
//            datList.add(new TupleDTO<>("合格率", BigDecimalUtil.percent(finished,all,2)));
//            stackGraphDTO.setData(datList);
//            dataList.add(stackGraphDTO);
//        });
        return StackGraphUtil.convertData(dataList, Boolean.TRUE);
    }
}




