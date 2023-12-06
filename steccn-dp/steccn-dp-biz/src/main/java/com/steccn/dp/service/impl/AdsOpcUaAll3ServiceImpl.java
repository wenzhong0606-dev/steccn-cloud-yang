package com.steccn.dp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.StackGraphDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.StackGraphUtil;
import com.steccn.dp.domain.AdsOpcUaAll3;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.dp.service.AdsOpcUaAll3Service;
import com.steccn.dp.mapper.AdsOpcUaAll3Mapper;
import com.steccn.dp.vo.DpValueDateVO;
import com.steccn.dp.vo.DpWeightVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.dto.SysConfigurationDTO;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【ads_opc_ua_all3(tagId包含Raw_YL.ZHM.fact_weigh的数据)】的数据库操作Service实现
* @createDate 2023-11-23 09:27:09
*/
@Service
@RequiredArgsConstructor
public class AdsOpcUaAll3ServiceImpl extends ServiceImpl<AdsOpcUaAll3Mapper, AdsOpcUaAll3>
    implements AdsOpcUaAll3Service{
    private final AdsOpcUaAll3Mapper adsOpcUaAll3Mapper;
    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;

    @Override
    public List<DpWeightVO> getWeighCount(TupleDTO<LocalDateTime, LocalDateTime> dto) {
        List<DpWeightVO> result = Lists.newArrayList();
        result = adsOpcUaAll3Mapper.weightCount(dto);
        if(CollUtil.isNotEmpty(result)){
            Set<String> refSet = result.stream().map(DpWeightVO::getTagId).collect(Collectors.toSet());
            Map<String, List<SysConfigurationDTO>> dpWeight = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confList(new TupleDTO<>("DP_WEIGHT", refSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            result.forEach(tmp->{
                tmp.setTagName(dpWeight.get(tmp.getTagId()).get(0).getRefContent());
            });
        }
        return result;
    }

    @Override
    public List<DpValueDateVO> weightCountAsDay(TupleDTO<LocalDateTime, LocalDateTime> dto) {
        List<DpValueDateVO> result = Lists.newArrayList();
        result = adsOpcUaAll3Mapper.weightCountAsDay(dto);
        return result;
    }

    @Override
    public QtLineChartBarDTO weightCountDashBoard(DpPmsOperateDTO dto) {
        List<DpWeightVO> result = Lists.newArrayList();
        result = adsOpcUaAll3Mapper.weightCountAsDayAndTag(new TupleDTO<>(dto.getStart().atTime(8,0,0),dto.getEnd().plusDays(1).atTime(7,59,59)));
        SteccnTableAssertUtil.notEmpty(result, "暂无数据");
        Set<String> refSet = result.stream().map(DpWeightVO::getTagId).collect(Collectors.toSet());
        Map<String, List<SysConfigurationDTO>> dpWeight = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confList(new TupleDTO<>("DP_WEIGHT", refSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //组图表
        List<StackGraphDTO<String, BigDecimal>> dataList = Lists.newArrayList();
        int dayCount = 0;
        if(dto.getType().equals(OaConstant.NO)){
            dayCount = LocalDateTimeUtil.betweenPeriod(dto.getStart(), dto.getEnd()).getDays();
        }else {
            dayCount =  dto.getEnd().getMonthValue() - dto.getStart().getMonthValue();
        }
        for (int i = 0; i <= dayCount; i++) {
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            LocalDate start = dto.getStart();
            //0是日报表，1是月报表
            if(dto.getType().equals(OaConstant.NO)){
                start = dto.getStart().plusDays(i);
                stackGraphDTO.setXAxis(start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }else {
                start = dto.getStart().plusMonths(i);
                stackGraphDTO.setXAxis(start.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            }
            LocalDate finalStart = start;
            List<DpWeightVO> resultTmp = result.stream().filter(v -> v.getDate().equals(finalStart))
                    .sorted(Comparator.comparing(DpWeightVO::getTagId)).collect(Collectors.toList());
            List<TupleDTO<String, BigDecimal>> datList = Lists.newArrayList();
            for(DpWeightVO tmp:resultTmp){
                String tmpName = dpWeight.get(tmp.getTagId()).get(0).getRefContent();
                //2#则跳过
                if(tmpName.contains("2#")){
                    continue;
                }
                //若是硅砂和碎玻璃则累加
                if(tmp.getTagId().equals("Raw_YL.ZHM.fact_weigh1_FLOAT")){
                    DpWeightVO twoResult = resultTmp.stream().filter(v -> v.getTagId().equals("Raw_YL.ZHM.fact_weigh2_FLOAT")).findFirst().orElse(null);
                    if(Objects.nonNull(twoResult)){
                        tmp.setValue(BigDecimalUtil.add(tmp.getValue(),twoResult.getValue()));
                    }
                }
                if(tmp.getTagId().equals("Raw_YL.ZHM.fact_weigh9_FLOAT")){
                    DpWeightVO twoResult = resultTmp.stream().filter(v -> v.getTagId().equals("Raw_YL.ZHM.fact_weigh10_FLOAT")).findFirst().orElse(null);
                    if(Objects.nonNull(twoResult)){
                        tmp.setValue(BigDecimalUtil.add(tmp.getValue(),twoResult.getValue()));
                    }
                }
                datList.add(new TupleDTO<>(tmpName, tmp.getValue()));
            }
            stackGraphDTO.setData(datList);
            dataList.add(stackGraphDTO);
        }
        return StackGraphUtil.convertData(dataList, Boolean.TRUE);
    }
}




