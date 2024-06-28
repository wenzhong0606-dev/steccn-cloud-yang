package com.steccn.dp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.NumberChineseFormatter;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.dp.domain.PullProduParCount;
import com.steccn.dp.domain.PullProduParDetail1;
import com.steccn.dp.domain.PullProduParDetail2;
import com.steccn.dp.mapper.PullProduParCountMapper;
import com.steccn.dp.service.PullProduParCountService;
import com.steccn.dp.service.PullProduParDetail1Service;
import com.steccn.dp.service.PullProduParDetail2Service;
import com.steccn.dp.vo.DpTheoryPullForceDataVO;
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
import java.util.Objects;

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
    private final PullProduParDetail1Service pullProduParDetail1Service;
    private final PullProduParDetail2Service pullProduParDetail2Service;

    @Override
    public DpTheoryPullForceVO getPullForce(LocalDate date) {
        DpTheoryPullForceVO result = new DpTheoryPullForceVO();
        List<DpTheoryPullForceDataVO> data = Lists.newArrayList();
        //获取理论拉引量
        List<PullProduParCount> model = list(new QueryWrapper<PullProduParCount>()
                .select("product_line AS productLine", "sum(id_value) AS idValue")
                .lambda()
                .ge(PullProduParCount::getDt1, date.atTime(8, 0, 0))
                .le(PullProduParCount::getDt1, date.plusDays(1).atTime(8, 0, 0))
                .groupBy(PullProduParCount::getProductLine));
        //获取平均拉引速度
        List<PullProduParDetail1> speed = pullProduParDetail1Service.list(new QueryWrapper<PullProduParDetail1>()
                .select("product_line AS productLine", "avg(id_value) AS idValue")
                .lambda()
                .ge(PullProduParDetail1::getDt, date.atTime(8, 0, 0))
                .le(PullProduParDetail1::getDt, date.plusDays(1).atTime(8, 0, 0))
                .groupBy(PullProduParDetail1::getProductLine));
        //获取板宽
        List<PullProduParDetail2> width = pullProduParDetail2Service.list(new QueryWrapper<PullProduParDetail2>()
                .select("product_line AS productLine", "avg(id_value) AS idValue")
                .lambda()
                .ge(PullProduParDetail2::getDt, date.atTime(8, 0, 0))
                .le(PullProduParDetail2::getDt, date.plusDays(1).atTime(8, 0, 0))
                .groupBy(PullProduParDetail2::getProductLine));
        //获取实际拉引量
        PmsPullForceQueryDTO queryDTO = new PmsPullForceQueryDTO();
        queryDTO.setStartDate(date);
        queryDTO.setEndDate(date);
        List<Integer> productLineIds = Lists.newArrayList(174,175,179,180,461,467,468,214);
        queryDTO.setProductLineIds(Sets.newHashSet(productLineIds));
        List<PmsPullForceDataVO> pmsPullForceDataVOS = FeignOptional.of(() -> remotePullForceFeignApi.getDataFromPeriods(queryDTO), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        if(CollUtil.isNotEmpty(pmsPullForceDataVOS)||CollUtil.isNotEmpty(model)){
            List<SysConfigurationDTO> cfg = FeignOptional.of(() -> remoteSysConfigurationFeignApi.infoByType("THEORY_PULL_FORCE"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //密度
            SysConfigurationDTO densityDTO = cfg.stream().filter(v -> v.getContent().equals("0")).findFirst().orElse(null);
            BigDecimal density = new BigDecimal(densityDTO.getRefContent());
            result.setGlassDensity(density);
            //8条产线计算
            for(int i=1;i<=8;i++){
                //理论拉引量 = 时长 * 速度 * 玻璃板宽 * 2.5 * 厚度
                DpTheoryPullForceDataVO tmp = new DpTheoryPullForceDataVO();
                String num = NumberChineseFormatter.format(i,false,false);
                tmp.setProductLineName("基片"+num+"产线");
                //厚度
                int finalI = i;
                BigDecimal thick = new BigDecimal(cfg.stream().filter(v -> v.getContent()
                        .equals(String.valueOf(finalI)))
                        .findFirst().orElse(null).getRefContent());
                tmp.setGlassThick(thick);
                //平均速度
                BigDecimal speedValue = speed.stream().filter(v -> v.getProductLine().equals(finalI)).findFirst().orElse(new PullProduParDetail1()).getIdValue();
                if(Objects.nonNull(speedValue)){
                    tmp.setAvgSpeed(BigDecimalUtil.setScale(speedValue,2));
                }
                //板宽
                BigDecimal widthValue = width.stream().filter(v -> v.getProductLine().equals(finalI)).findFirst().orElse(new PullProduParDetail2()).getIdValue();
                if(Objects.nonNull(widthValue)){
                    tmp.setGlassWidth(BigDecimalUtil.multiply(widthValue,BigDecimal.valueOf(1000),2));
                }
                //理论拉引量
                BigDecimal theoryNum = model.stream().filter(v -> v.getProductLine().equals(finalI)).findFirst().orElse(new PullProduParCount()).getIdValue();
                theoryNum = BigDecimalUtil.multiply(BigDecimalUtil.multiply(theoryNum,density),thick,2);
                tmp.setTheoryPullForce(theoryNum);
                //实际拉引量
                BigDecimal actNum = pmsPullForceDataVOS.stream()
                        .filter(v -> v.getProductLineId().equals(productLineIds.get(finalI - 1))
                                && Objects.nonNull(v.getQuantity()))
                        .map(PmsPullForceDataVO::getQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if(Objects.nonNull(actNum)){
                    tmp.setActualPullForce(BigDecimalUtil.setScale(actNum,2));
                }
                data.add(tmp);
                result.setData(data);
            }
        }else {
            throw new BusinessException("数据为空");
        }
        return result;
    }
}




