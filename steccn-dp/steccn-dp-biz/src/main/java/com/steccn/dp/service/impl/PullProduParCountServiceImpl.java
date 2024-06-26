package com.steccn.dp.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.dp.domain.PullProduParCount;
import com.steccn.dp.mapper.PullProduParDetail1Mapper;
import com.steccn.dp.mapper.PullProduParDetail2Mapper;
import com.steccn.dp.service.PullProduParCountService;
import com.steccn.dp.mapper.PullProduParCountMapper;
import com.steccn.dp.vo.DpTheoryPullForceVO;
import com.steccn.platform.dto.SysConfigurationDTO;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import com.steccn.pms.dto.PmsPullForceQueryDTO;
import com.steccn.pms.feign.RemotePullForceFeignApi;
import com.steccn.pms.vo.PmsPullForceDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
* @author Admin
* @description 针对表【pull_produ_par_count】的数据库操作Service实现
* @createDate 2024-06-24 16:15:14
*/
@Service
@DS("ch")
@RequiredArgsConstructor
public class PullProduParCountServiceImpl extends ServiceImpl<PullProduParCountMapper, PullProduParCount>
    implements PullProduParCountService{
    private final RemotePullForceFeignApi remotePullForceFeignApi;
    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;
    private final PullProduParDetail1Mapper pullProduParDetail1Mapper;
    private final PullProduParDetail2Mapper pullProduParDetail2Mapper;

    @Override
    public List<DpTheoryPullForceVO> getPullForce(LocalDate date) {
        List<DpTheoryPullForceVO> results = Lists.newArrayList();
        //获取理论拉引量
        List<PullProduParCount> model = list(new QueryWrapper<PullProduParCount>()
                .select("product_line AS field1", "Sum(id_value) AS fields")
                .lambda()
                .ge(PullProduParCount::getDt1, date.atTime(8, 0, 0))
                .le(PullProduParCount::getDt1, date.plusDays(1).atTime(8, 0, 0))
                .groupBy(PullProduParCount::getProductLine));
        //获取实际拉引量
        PmsPullForceQueryDTO queryDTO = new PmsPullForceQueryDTO();
        queryDTO.setStartDate(date);
        queryDTO.setEndDate(date);
        List<Integer> productLineIds = Lists.newArrayList(174,175,179,180,461,467,468,214);
        queryDTO.setProductLineIds(Sets.newHashSet(productLineIds));
        List<PmsPullForceDataVO> pmsPullForceDataVOS = FeignOptional.of(() -> remotePullForceFeignApi.getDataFromPeriods(queryDTO), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        if(CollUtil.isNotEmpty(pmsPullForceDataVOS)&&CollUtil.isNotEmpty(model)){
            List<SysConfigurationDTO> cfg = FeignOptional.of(() -> remoteSysConfigurationFeignApi.infoByType("THEORY_PULL_FORCE"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //密度
            SysConfigurationDTO densityDTO = cfg.stream().filter(v -> v.getContent().equals("0")).findFirst().orElse(null);
            BigDecimal density = new BigDecimal(densityDTO.getOtherContent());
            //8条产线计算
            for(int i=1;i<=8;i++){

            }
        }else {
            throw new BusinessException("数据为空");
        }
        return null;
    }
}




