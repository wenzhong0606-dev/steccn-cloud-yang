package com.steccn.data.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.data.domain.MesEqpSignal;
import com.steccn.data.service.HbCollectService;
import com.steccn.data.service.MesEqpSignalService;
import com.steccn.data.mapper.MesEqpSignalMapper;
import com.steccn.mes.dto.EqpReportDTO;
import com.steccn.mes.feign.RemoteMesFeignApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;

/**
* @author Admin
* @description 针对表【mes_eqp_signal】的数据库操作Service实现
* @createDate 2023-04-25 10:43:42
*/
@Service
@RequiredArgsConstructor
public class MesEqpSignalServiceImpl extends ServiceImpl<MesEqpSignalMapper, MesEqpSignal>
    implements MesEqpSignalService{
    private final RemoteMesFeignApi remoteMesFeignApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveReport() {
        List<MesEqpSignal> list = list(new LambdaQueryWrapper<MesEqpSignal>().select(MesEqpSignal::getCode).groupBy(MesEqpSignal::getCode));
        for(MesEqpSignal eqp:list){
            List<MesEqpSignal> eqpList = list(new LambdaQueryWrapper<MesEqpSignal>()
                    .eq(MesEqpSignal::getCode, eqp.getCode())
                    .orderByAsc(MesEqpSignal::getTimestamp));
            for(int i = 0; i < eqpList.size(); i++){
                if(i==0){
                    continue;
                }
                MesEqpSignal signalTmp = eqpList.get(i);
                MesEqpSignal signalTmp2 = eqpList.get(i-1);
                EqpReportDTO reportDTO = new EqpReportDTO();
                reportDTO.setTimestamp(signalTmp.getTimestamp());
                reportDTO.setEqpCode(signalTmp.getCode());
                reportDTO.setState(signalTmp.getValue());
                Duration duration2 = Duration.between(eqpList.get(i-1).getTimestamp(),signalTmp.getTimestamp());
                BigDecimal hours2 = BigDecimalUtil.divide(BigDecimal.valueOf(duration2.getSeconds()),BigDecimal.valueOf(60));
                reportDTO.setValue(BigDecimalUtil.setScale(hours2,2));
                FeignOptional.of(() -> remoteMesFeignApi.changeReportData(reportDTO), FeignConstant.STECCN_MES_SERVICE_NAME).get();
            }
        }
        return true;
    }
}




