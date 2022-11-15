package com.steccn.pms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.google.common.collect.Maps;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.domain.PmsPullForce;
import com.steccn.pms.dto.ProducePassRateDTO;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.service.PmsPullForceService;
import com.steccn.pms.service.ProducePassRateService;
import com.steccn.pms.vo.PassRateDateVo;
import com.steccn.pms.vo.PmsProducePlanVO;
import com.steccn.pms.vo.PmsReportWorkGroupVO;
import com.steccn.pms.vo.ProducePassRateVO;
import lombok.RequiredArgsConstructor;
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

        //分组获取产量数据
        List<PmsReportWorkGroupVO> pmsReportWorkGroupVOList = getPmsReportWorkGroupVOList(localDateList,dto);

        QueryWrapper<PmsPullForce> queryWrapper = getQueryWrapper(dto);
        List<PmsPullForce> pmsPullForceList = pmsPullForceService.list(queryWrapper);
        localDateList.remove(localDateList.size() -1);
        List<ProducePassRateVO> producePassRateVOList = new ArrayList<>();
        for (LocalDate localDate : localDateList) {
            //过滤出指定日期的产量
            List<PmsReportWorkGroupVO> reportWorkList = pmsReportWorkGroupVOList.stream()
                    .filter(workGroupVO -> workGroupVO.getGroupDate().equals(localDate)).collect(Collectors.toList());

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
                            .filter(pmsPullForce -> pmsPullForce.getPullForceDate().equals(reportWorkGroupVO.getGroupDate())
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

    private List<PmsReportWorkGroupVO> getPmsReportWorkGroupVOList(List<LocalDate> localDateList,ProducePassRateDTO dto) {
        LocalTime localTime = LocalTime.of(8,0,0);
        LocalDateTime beginDateTime = LocalDateTime.of(dto.getBeginDate(),localTime);
        LocalDateTime endDateTime = LocalDateTime.of(dto.getEndDate(),localTime);

        List<LocalDate> dateList = new ArrayList<>(localDateList);
        Map<LocalDate, PassRateDateVo> dateMap = Maps.newHashMap();
        while (!CollectionUtils.isEmpty(dateList)) {
            PassRateDateVo rateDateVo = new PassRateDateVo();
            LocalDate endDate = localDateList.get(localDateList.size() - 1);
            LocalDate startDate = localDateList.get(localDateList.size() - 2);

            rateDateVo.setStartDateTime(LocalDateTime.of(startDate, localTime));
            rateDateVo.setEndDateTime(LocalDateTime.of(endDate, localTime));
            localDateList.remove(endDate);
            if (localDateList.size() == 1) {
                localDateList.remove(startDate);
            }
            dateMap.put(startDate,rateDateVo);
        }

        List<PmsReportWorkGroupVO> reportWorkGroupVOList = pmsReportWorkMapper.selectAllGroupByDate(beginDateTime, endDateTime);
        Set<Integer> childIdList = reportWorkGroupVOList.stream().map(PmsReportWorkGroupVO::getProductLineId).collect(Collectors.toSet());
        Map<Integer, SysOfficeVO> sysOfficeMap = remoteOfficeFeign.getParentOfficeMap(new ArrayList<>(childIdList)).getData();
        //获取分组日期
        for (PmsReportWorkGroupVO pmsReportWorkGroupVO : reportWorkGroupVOList) {
            LocalDateTime dateTime = pmsReportWorkGroupVO.getDate();
            for (Map.Entry<LocalDate, PassRateDateVo> entry : dateMap.entrySet()) {
                LocalDateTime startTime = entry.getValue().getStartDateTime();
                LocalDateTime endTime = entry.getValue().getStartDateTime();
                boolean bool = dateTime.isBefore(startTime) || dateTime.isEqual(startTime);
                if (bool && dateTime.isAfter(endTime)) {
                    pmsReportWorkGroupVO.setGroupDate(entry.getKey());
                }
            }
            //根据产线id获取车间id
            if (Objects.nonNull(sysOfficeMap)) {
                pmsReportWorkGroupVO.setWorkshopId(sysOfficeMap.get(pmsReportWorkGroupVO.getProductLineId()).getParentId());
            }
        }

        Map<String, List<PmsReportWorkGroupVO>> groupBy = reportWorkGroupVOList
                .stream()
                .collect(Collectors.groupingBy(reportWorkGroupVO -> reportWorkGroupVO.getGroupDate() + "," +
                        reportWorkGroupVO.getWorkshopId() + "," + reportWorkGroupVO.getProductLineId() + "," + reportWorkGroupVO.getClassesId()));

        List<PmsReportWorkGroupVO> pmsReportWorkGroupVOList = new ArrayList<>();
        for (Map.Entry<String, List<PmsReportWorkGroupVO>> entry : groupBy.entrySet()) {
            String[] splits = entry.getKey().split(",");
            LocalDate groupDate = LocalDate.parse(splits[0]);
            Integer workshopId = null;
            if (StringUtils.isNotBlank(splits[1])) {
                workshopId = Integer.parseInt(splits[1]);
            }
            Integer productLineId = Integer.parseInt(splits[2]);
            Long classesId = Long.parseLong(splits[3]);
            BigDecimal total = entry.getValue().stream().map(PmsReportWorkGroupVO::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);

            PmsReportWorkGroupVO pmsReportWorkGroupVO = new PmsReportWorkGroupVO();
            pmsReportWorkGroupVO.setGroupDate(groupDate);
            pmsReportWorkGroupVO.setWorkshopId(workshopId);
            pmsReportWorkGroupVO.setProductLineId(productLineId);
            pmsReportWorkGroupVO.setClassesId(classesId);
            pmsReportWorkGroupVO.setQuantity(total);
            pmsReportWorkGroupVOList.add(pmsReportWorkGroupVO);
        }

        return pmsReportWorkGroupVOList;
    }
}
