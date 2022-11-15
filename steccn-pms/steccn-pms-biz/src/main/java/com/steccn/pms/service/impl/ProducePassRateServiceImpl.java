package com.steccn.pms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.domain.PmsPullForce;
import com.steccn.pms.dto.ProducePassRateDTO;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.service.PmsProducePlanService;
import com.steccn.pms.service.PmsPullForceService;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.pms.service.ProducePassRateService;
import com.steccn.pms.vo.PmsPullForceVO;
import com.steccn.pms.vo.PmsReportWorkGroupVO;
import com.steccn.pms.vo.ProducePassRateVO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenhuan
 * @date 2022/11/9 16:27
 */
@RequiredArgsConstructor
@Service
public class ProducePassRateServiceImpl implements ProducePassRateService {

    private final PmsPullForceService pmsPullForceService;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final PmsReportWorkMapper pmsReportWorkMapper;

    private final RemoteDictFeignApi remoteDictFeignApi;


    @Override
    public List<ProducePassRateVO> getProducePassRateGroup(ProducePassRateDTO dto) {

        //获取日期
        List<LocalDate> localDateList = LocalDateUtil.getLocalDateByStartEndDate(dto.getBeginDate(),dto.getEndDate());

        LocalDateTime beginDateTime = LocalDateTime.of(dto.getBeginDate(), LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(dto.getEndDate(), LocalTime.MAX);

        List<PmsReportWorkGroupVO> reportWorkGroupVOList = pmsReportWorkMapper.selectAllGroupByDate(beginDateTime, endDateTime);


        Set<Integer> childIdList = reportWorkGroupVOList.stream().map(PmsReportWorkGroupVO::getProductLineId).collect(Collectors.toSet());
        Map<Integer, SysOfficeVO> sysOfficeMap = remoteOfficeFeign.getParentOfficeMap(new ArrayList<>(childIdList)).getData();
        //根据产线id获取车间id
        if (Objects.nonNull(sysOfficeMap)) {
            for (PmsReportWorkGroupVO reportWorkGroupVO : reportWorkGroupVOList){
                reportWorkGroupVO.setWorkshopId(sysOfficeMap.get(reportWorkGroupVO.getProductLineId()).getParentId());
            }
        }

        QueryWrapper<PmsPullForce> queryWrapper = getQueryWrapper(dto);
        List<PmsPullForce> pmsPullForceList = pmsPullForceService.list(queryWrapper);

        List<ProducePassRateVO> producePassRateVOList = new ArrayList<>();
        for (LocalDate localDate : localDateList) {
            //过滤出指定日期的产量
            List<PmsReportWorkGroupVO> reportWorkList = reportWorkGroupVOList.stream()
                    .filter(workGroupVO -> workGroupVO.getDate().equals(localDate)).collect(Collectors.toList());


            if (CollectionUtils.isEmpty(reportWorkList)) {
                ProducePassRateVO producePassRateVO = new ProducePassRateVO();
                producePassRateVO.setDate(localDate);
                producePassRateVOList.add(producePassRateVO);
                continue;
            }

            for (PmsReportWorkGroupVO reportWorkGroupVO : reportWorkList) {
                ProducePassRateVO producePassRateVO = new ProducePassRateVO();
                producePassRateVO.setDate(localDate);
                producePassRateVO.setClassesId(reportWorkGroupVO.getClassesId());
                producePassRateVO.setProductLineId(reportWorkGroupVO.getProductLineId());
                producePassRateVO.setWorkshopId(reportWorkGroupVO.getWorkshopId());

                if (CollectionUtils.isNotEmpty(pmsPullForceList)) {
                    Optional<PmsPullForce> optional = pmsPullForceList.stream()
                            .filter(pmsPullForce -> pmsPullForce.getPullForceDate().equals(reportWorkGroupVO.getDate())
                                    && pmsPullForce.getClassesId().equals(reportWorkGroupVO.getClassesId())
                                    && Objects.nonNull(reportWorkGroupVO.getWorkshopId())
                                    && pmsPullForce.getWorkshopId().equals(reportWorkGroupVO.getWorkshopId())
                                    && pmsPullForce.getProductLineId().equals(reportWorkGroupVO.getProductLineId())).findAny();
                    if (optional.isPresent()) {
                        producePassRateVO.setPassRate(BigDecimalUtil.divide(reportWorkGroupVO.getQuantity(),optional.get().getQuantity(),4));
                    } else {
                        producePassRateVO.setPassRate(BigDecimal.ZERO);
                    }
                } else {
                    producePassRateVO.setPassRate(BigDecimal.ZERO);
                }
                producePassRateVOList.add(producePassRateVO);
            }

        }
        Set<Integer> sysOfficeIdSet = producePassRateVOList.stream().map(ProducePassRateVO::getWorkshopId).collect(Collectors.toSet());
        Set<Integer> productLineIdList = producePassRateVOList.stream().map(ProducePassRateVO::getProductLineId).collect(Collectors.toSet());
        Set<Long> classesIdList = producePassRateVOList.stream().map(ProducePassRateVO::getClassesId).collect(Collectors.toSet());
        sysOfficeIdSet.addAll(productLineIdList);

        Map<Integer, SysOfficeVO> sysOfficeVoMap = remoteOfficeFeign.listById(sysOfficeIdSet).getData();
        SysOfficeVO defaultSysOfficeVO = new SysOfficeVO();
        Map<Long, String> classesMap = remoteDictFeignApi.listByIdCh(classesIdList).getData();
        producePassRateVOList.forEach(producePassRateVO -> {
            producePassRateVO.setClassesName(classesMap.get(producePassRateVO.getClassesId()));
            producePassRateVO.setWorkshopName(sysOfficeVoMap.getOrDefault(producePassRateVO.getWorkshopId(),defaultSysOfficeVO).getOfficeName());
            producePassRateVO.setProductLineName(sysOfficeVoMap.getOrDefault(producePassRateVO.getProductLineId(),defaultSysOfficeVO).getOfficeName());
        });

        return producePassRateVOList;
    }

    private QueryWrapper<PmsPullForce> getQueryWrapper(ProducePassRateDTO dto){

        QueryWrapper<PmsPullForce> queryWrapper = new QueryWrapper<PmsPullForce>();
        queryWrapper.select("pull_force_date as pullForceDate,workshop_id as workshopId,product_line_id ,classes_id as classesId, sum(quantity) as quantity");

        List<SFunction<PmsPullForce,?>> sFunctionList = new ArrayList<>();
        sFunctionList.add(PmsPullForce::getPullForceDate);
        sFunctionList.add(PmsPullForce::getWorkshopId);
        sFunctionList.add(PmsPullForce::getProductLineId);
        sFunctionList.add(PmsPullForce::getClassesId);

        queryWrapper.lambda()
                .ge(PmsPullForce::getPullForceDate,dto.getBeginDate())
                .le(PmsPullForce::getPullForceDate,dto.getEndDate()).groupBy(sFunctionList);

        return queryWrapper;
    }
}
