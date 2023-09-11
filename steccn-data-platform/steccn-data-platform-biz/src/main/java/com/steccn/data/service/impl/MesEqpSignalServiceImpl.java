package com.steccn.data.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.data.domain.MesEqpSignal;
import com.steccn.data.dto.OperationRecordsDTO;
import com.steccn.data.mapper.MesEqpSignalMapper;
import com.steccn.data.service.MesEqpSignalService;
import com.steccn.data.vo.OperationRecordVO;
import com.steccn.data.vo.OperationStateVO;
import com.steccn.data.vo.OperationStatusVO;
import com.steccn.mes.feign.RemoteMesFeignApi;
import com.steccn.mes.vo.MesEqpVO;
import com.steccn.oa.constant.OaConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
* @author Admin
* @description 针对表【mes_eqp_signal】的数据库操作Service实现
* @createDate 2023-09-05 09:19:04
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class MesEqpSignalServiceImpl extends ServiceImpl<MesEqpSignalMapper, MesEqpSignal>
    implements MesEqpSignalService{
    private final RemoteMesFeignApi remoteMesFeignApi;

    @Override
    public OperationStatusVO getEqpStatus(OperationRecordsDTO dto) {
        List<MesEqpSignal> mesEqpSignals = list(new LambdaQueryWrapper<MesEqpSignal>()
                .eq(MesEqpSignal::getCode, dto.getEqpCode())
                .between(MesEqpSignal::getTimestamp, dto.getStartTime(), dto.getEndTime())
                .orderByAsc(MesEqpSignal::getTimestamp));
        OperationStatusVO result = new OperationStatusVO();
        if(CollUtil.isEmpty(mesEqpSignals)){
            result.setStopNum(0L);
            result.setStopTime("0秒");
            return result;
        }
        //停机次数
        long stopNum = mesEqpSignals.stream().filter(v -> v.getValue().equals(OaConstant.NO)).count();
        result.setStopNum(stopNum);
        List<OperationStateVO> states = Lists.newArrayList();
        long allTime = DateUtil.between(Date.from(mesEqpSignals.get(0).getTimestamp().atZone(ZoneId.systemDefault()).toInstant()),Date.from(dto.getEndTime().atZone(ZoneId.systemDefault()).toInstant()), DateUnit.MS);
        long miles = 0;
        BigDecimal percentAll = BigDecimal.ZERO;
        //停机时间
        for(int i = 0; i < mesEqpSignals.size(); i++){
            MesEqpSignal tmp = mesEqpSignals.get(i);
            BigDecimal percent = BigDecimal.ZERO;
            if(tmp.getValue().equals(OaConstant.NO)){
                //如果下一个没有
                if(i+1 == mesEqpSignals.size()){
                    miles = miles + DateUtil.between(Date.from(tmp.getTimestamp().atZone(ZoneId.systemDefault()).toInstant()),Date.from(dto.getEndTime().atZone(ZoneId.systemDefault()).toInstant()), DateUnit.MS);
                    percent = BigDecimalUtil.subtract(new BigDecimal(1),percentAll);
                    if(percent.compareTo(new BigDecimal(0))==-1){
                        percent = BigDecimal.ZERO;
                    }
                }else {
                    miles = miles + DateUtil.between(Date.from(tmp.getTimestamp().atZone(ZoneId.systemDefault()).toInstant()),Date.from(mesEqpSignals.get(i+1).getTimestamp().atZone(ZoneId.systemDefault()).toInstant()), DateUnit.MS);
                    percent = BigDecimalUtil.divide(new BigDecimal(miles),new BigDecimal(allTime));
                    percentAll = BigDecimalUtil.add(percentAll,percent);
                }
            }
            OperationStateVO state = new OperationStateVO();
            state.setState(tmp.getValue());
            state.setTime(tmp.getTimestamp());
            state.setPercent(percent);
            states.add(state);
        }
        String formatSecond = DateUtil.formatBetween(miles, BetweenFormatter.Level.SECOND);
        result.setStopTime(formatSecond);
        result.setState(states);
        return result;
    }

    @Override
    public Page<OperationRecordVO> getEqpRecord(OperationRecordsDTO dto) {
        //2.条件查询
        LambdaQueryWrapper<MesEqpSignal> condition = new LambdaQueryWrapper<MesEqpSignal>()
                .eq(MesEqpSignal::getCode, dto.getEqpCode())
                .between(MesEqpSignal::getTimestamp, dto.getStartTime(), dto.getEndTime())
                .orderByAsc(MesEqpSignal::getTimestamp);
        MesEqpVO mesEqpVO = FeignOptional.of(() -> remoteMesFeignApi.getByCode(dto.getEqpCode()), FeignConstant.STECCN_MES_SERVICE_NAME).get();
        //3.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //4.查询分页数据
        IPage<MesEqpSignal> resPage = page(page, condition);
        //5.拷贝page信息
        BeanUtils.copyProperties(resPage, page);
        //6.拷贝返回的信息
        List<OperationRecordVO> resRecords = Lists.newArrayList();
        if (CollUtil.isNotEmpty(resPage.getRecords())) {
            for (MesEqpSignal mesEqpSignal : resPage.getRecords()) {
                OperationRecordVO vo = new OperationRecordVO();
                BeanUtils.copyProperties(mesEqpSignal, vo);
                vo.setEqpName(mesEqpVO.getEquipmentName());
                resRecords.add(vo);
            }
        }
        //7.设置数据
        page.setRecords(resRecords);
        return page;
    }
}




