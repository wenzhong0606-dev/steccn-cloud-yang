package com.steccn.dp.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.dp.domain.AdsOpcUaAll3;
import com.steccn.dp.service.AdsOpcUaAll3Service;
import com.steccn.dp.mapper.AdsOpcUaAll3Mapper;
import com.steccn.dp.vo.DpValueDateVO;
import com.steccn.dp.vo.DpWeightVO;
import com.steccn.platform.dto.SysConfigurationDTO;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
}




