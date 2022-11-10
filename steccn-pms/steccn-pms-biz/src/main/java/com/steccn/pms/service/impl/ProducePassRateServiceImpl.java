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
        List<LocalDate> localDateList = LocalDateUtil.getLocalDateByStartEndDate(dto.getBeginDate().toLocalDate(),dto.getEndDate().toLocalDate());

        List<PmsReportWorkGroupVO> reportWorkGroupVOList = pmsReportWorkMapper.selectAllGroupByDate(
                dto.getProductionLineIdList()
                , dto.getBeginDate(), dto.getEndDate()
                ,dto.getGroupType());

        //如果根据车间分组，获取车间分组后的数据
        if (dto.getGroupType().equals(3)) {
             reportWorkGroupVOList = groupWorkshopId(reportWorkGroupVOList);
        }

        QueryWrapper<PmsPullForce> queryWrapper = getQueryWrapper(dto);
        List<PmsPullForce> pmsPullForceList = pmsPullForceService.list(queryWrapper);

        List<ProducePassRateVO> producePassRateVOList = new ArrayList<>();
        for (LocalDate localDate : localDateList) {
            //过滤出指定日期的产量
            List<PmsReportWorkGroupVO> reportWorkList = reportWorkGroupVOList.stream()
                    .filter(workGroupVO -> workGroupVO.getDate().equals(localDate)).collect(Collectors.toList());
            for (PmsReportWorkGroupVO reportWorkGroupVO : reportWorkList) {
                ProducePassRateVO producePassRateVO = new ProducePassRateVO();

                //获取指定分组id、指定日期的拉引量
                Optional<PmsPullForce> pullForce = getPullForce(pmsPullForceList,dto.getGroupType(),reportWorkGroupVO);

                producePassRateVO.setDate(localDate);
                producePassRateVO.setGroupId(reportWorkGroupVO.getGroupId());
                if (pullForce.isPresent() && Objects.nonNull(pullForce.get().getQuantity())){
                    producePassRateVO.setPassRate(reportWorkGroupVO.getQuantity().divide(pullForce.get().getQuantity()));
                }
                producePassRateVOList.add(producePassRateVO);
            }


        }
        return producePassRateVOList;
    }

    private Optional<PmsPullForce> getPullForce(List<PmsPullForce> pmsPullForceList,Integer groupType, PmsReportWorkGroupVO reportWorkGroupVO){
        Optional<PmsPullForce> pullForce;
        if (groupType.equals(0)) {
             pullForce = pmsPullForceList.stream().filter(pmsPullForce ->
                    pmsPullForce.getPullForceDate().equals(reportWorkGroupVO.getDate()) && pmsPullForce.getClassesId().equals(reportWorkGroupVO.getGroupId())
            ).findAny();
        } else if (groupType.equals(1)) {
            pullForce = pmsPullForceList.stream().filter(pmsPullForce ->
                    pmsPullForce.getPullForceDate().equals(reportWorkGroupVO.getDate()) && reportWorkGroupVO.getGroupId().equals(pmsPullForce.getProductLineId().longValue())
            ).findAny();
        } else {
            pullForce = pmsPullForceList.stream().filter(pmsPullForce ->
                    pmsPullForce.getPullForceDate().equals(reportWorkGroupVO.getDate()) && reportWorkGroupVO.getGroupId().equals(pmsPullForce.getWorkshopId().longValue())
            ).findAny();
        }
        return pullForce;
    }

    private List<PmsReportWorkGroupVO> groupWorkshopId(List<PmsReportWorkGroupVO> reportWorkGroupVOList){
        Set<Long> childIdList = reportWorkGroupVOList.stream().map(PmsReportWorkGroupVO::getGroupId).collect(Collectors.toSet());
        Map<Long, SysOfficeVO> sysOfficeMap = remoteOfficeFeign.getParentOfficeMap(new ArrayList<>(childIdList)).getData();
        //给产线id转成车间id
        for (PmsReportWorkGroupVO reportWorkGroupVO : reportWorkGroupVOList){
            reportWorkGroupVO.setGroupId(sysOfficeMap.get(reportWorkGroupVO.getGroupId()).getParentId().longValue());
        }
        Map<Long, List<PmsReportWorkGroupVO>> groupBy = reportWorkGroupVOList.stream().collect(Collectors.groupingBy(PmsReportWorkGroupVO::getGroupId));

        List<PmsReportWorkGroupVO> pmsReportWorkGroupVOList = new ArrayList<>();
        for (Map.Entry<Long, List<PmsReportWorkGroupVO>> entry : groupBy.entrySet()) {
            LocalDate date = entry.getValue().get(0).getDate();
            BigDecimal total = entry.getValue().stream().map(PmsReportWorkGroupVO::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);

            PmsReportWorkGroupVO reportWorkGroupVO = new PmsReportWorkGroupVO();
            reportWorkGroupVO.setGroupId(entry.getKey());
            reportWorkGroupVO.setDate(date);
            reportWorkGroupVO.setQuantity(total);

            pmsReportWorkGroupVOList.add(reportWorkGroupVO);
        }
        return pmsReportWorkGroupVOList;
    }

    private QueryWrapper<PmsPullForce> getQueryWrapper(ProducePassRateDTO dto){

        QueryWrapper<PmsPullForce> queryWrapper = new QueryWrapper<PmsPullForce>();
        List<SFunction<PmsPullForce,?>> sFunctionList = new ArrayList<>();

        //根据分组类型查询字段
        if (dto.getGroupType().equals(0)) {
            queryWrapper.select("pull_force_date as date,classes_id as classesId, sum(quantity) as quantity");
            sFunctionList.add(PmsPullForce::getPullForceDate);
            sFunctionList.add(PmsPullForce::getClassesId);
        } else if (dto.getGroupType().equals(1)){
            queryWrapper.select("pull_force_date as date,production_line_id as productionLineId, sum(quantity) as quantity");
            sFunctionList.add(PmsPullForce::getPullForceDate);
            sFunctionList.add(PmsPullForce::getProductLineId);
        } else {
            queryWrapper.select("pull_force_date as date,workshop_id as workshopId, sum(quantity) as quantity");
            sFunctionList.add(PmsPullForce::getPullForceDate);
            sFunctionList.add(PmsPullForce::getWorkshopId);
        }
        if (CollectionUtils.isNotEmpty(dto.getProductionLineIdList())) {
            queryWrapper.lambda().in(PmsPullForce::getProductLineId,dto.getProductionLineIdList());
        }
        if (CollectionUtils.isNotEmpty(dto.getClassIdList())) {
            queryWrapper.lambda().in(PmsPullForce::getClassesId,dto.getClassIdList());
        }

        queryWrapper.lambda()
                .ge(PmsPullForce::getPullForceDate,dto.getBeginDate())
                .le(PmsPullForce::getPullForceDate,dto.getEndDate()).groupBy(sFunctionList);

        return queryWrapper;
    }
}
