package com.steccn.pms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.steccn.common.util.LocalDateUtil;
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

    private final PmsReportWorkService pmsReportWorkService;

    private final PmsPullForceService pmsPullForceService;

    private final RemoteSpareFeignApi remoteSpareFeignApi;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final PmsReportWorkMapper pmsReportWorkMapper;


    @Override
    public List<ProducePassRateVO> getProducePassRateGroup(ProducePassRateDTO dto) {

        //获取日期
        List<LocalDate> localDateList = LocalDateUtil.getLocalDateByStartEndDate(dto.getBeginDate(),dto.getEndDate());

        LocalDateTime beginDateTime = LocalDateTime.of(dto.getBeginDate(), LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(dto.getBeginDate(), LocalTime.MAX);

        List<PmsReportWorkGroupVO> reportWorkGroupVOList = pmsReportWorkMapper.selectAllGroupByDate(beginDateTime, endDateTime);


        Set<Integer> childIdList = reportWorkGroupVOList.stream().map(PmsReportWorkGroupVO::getProductLineId).collect(Collectors.toSet());
        Map<Integer, SysOfficeVO> sysOfficeMap = remoteOfficeFeign.getParentOfficeMap(new ArrayList<>(childIdList)).getData();
        //根据产线id获取车间id
        for (PmsReportWorkGroupVO reportWorkGroupVO : reportWorkGroupVOList){
            reportWorkGroupVO.setWorkshopId(sysOfficeMap.get(reportWorkGroupVO.getProductLineId()).getParentId());
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
                                    && pmsPullForce.getWorkshopId().equals(reportWorkGroupVO.getWorkshopId())
                                    && pmsPullForce.getProductLineId().equals(reportWorkGroupVO.getProductLineId())).findAny();
                    if (optional.isPresent()) {
                        producePassRateVO.setPassRate(reportWorkGroupVO.getQuantity().divide(optional.get().getQuantity()));
                    } else {
                        producePassRateVO.setPassRate(BigDecimal.ZERO);
                    }
                } else {
                    producePassRateVO.setPassRate(BigDecimal.ZERO);
                }
                producePassRateVOList.add(producePassRateVO);
            }


        }
        return producePassRateVOList;
    }

//    private Optional<PmsPullForce> getPullForce(List<PmsPullForce> pmsPullForceList,Integer groupType, PmsReportWorkGroupVO reportWorkGroupVO){
//        Optional<PmsPullForce> pullForce;
//        if (groupType.equals(0)) {
//             pullForce = pmsPullForceList.stream().filter(pmsPullForce ->
//                    pmsPullForce.getPullForceDate().equals(reportWorkGroupVO.getDate()) && pmsPullForce.getClassesId().equals(reportWorkGroupVO.getGroupId())
//            ).findAny();
//        } else if (groupType.equals(1)) {
//            pullForce = pmsPullForceList.stream().filter(pmsPullForce ->
//                    pmsPullForce.getPullForceDate().equals(reportWorkGroupVO.getDate()) && reportWorkGroupVO.getGroupId().equals(pmsPullForce.getProductLineId().longValue())
//            ).findAny();
//        } else {
//            pullForce = pmsPullForceList.stream().filter(pmsPullForce ->
//                    pmsPullForce.getPullForceDate().equals(reportWorkGroupVO.getDate()) && reportWorkGroupVO.getGroupId().equals(pmsPullForce.getWorkshopId().longValue())
//            ).findAny();
//        }
//        return pullForce;
//    }

//    private List<PmsReportWorkGroupVO> groupWorkshopId(List<PmsReportWorkGroupVO> reportWorkGroupVOList){
//        Set<Integer> childIdList = reportWorkGroupVOList.stream().map(PmsReportWorkGroupVO::getProductLineId).collect(Collectors.toSet());
//        Map<Integer, SysOfficeVO> sysOfficeMap = remoteOfficeFeign.getParentOfficeMap(new ArrayList<>(childIdList)).getData();
//        //根据产线id获取车间id
//        for (PmsReportWorkGroupVO reportWorkGroupVO : reportWorkGroupVOList){
//            reportWorkGroupVO.setWorkshopId(sysOfficeMap.get(reportWorkGroupVO.getProductLineId()).getParentId());
//        }
//        Map<Long, List<PmsReportWorkGroupVO>> groupBy = reportWorkGroupVOList.stream().collect(Collectors.groupingBy(PmsReportWorkGroupVO::getGroupId));
//
//        List<PmsReportWorkGroupVO> pmsReportWorkGroupVOList = new ArrayList<>();
//        for (Map.Entry<Long, List<PmsReportWorkGroupVO>> entry : groupBy.entrySet()) {
//            LocalDate date = entry.getValue().get(0).getDate();
//            BigDecimal total = entry.getValue().stream().map(PmsReportWorkGroupVO::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
//
//            PmsReportWorkGroupVO reportWorkGroupVO = new PmsReportWorkGroupVO();
//            reportWorkGroupVO.setGroupId(entry.getKey());
//            reportWorkGroupVO.setDate(date);
//            reportWorkGroupVO.setQuantity(total);
//
//            pmsReportWorkGroupVOList.add(reportWorkGroupVO);
//        }
//        return pmsReportWorkGroupVOList;
//    }

    private QueryWrapper<PmsPullForce> getQueryWrapper(ProducePassRateDTO dto){

        QueryWrapper<PmsPullForce> queryWrapper = new QueryWrapper<PmsPullForce>();
        queryWrapper.select("pull_force_date as date,workshop_id as workshopId,production_line_id as productionLineId,classes_id as classesId, sum(quantity) as quantity");

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
