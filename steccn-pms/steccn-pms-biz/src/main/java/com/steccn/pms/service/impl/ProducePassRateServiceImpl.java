package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.platform.dto.SysOfficeDTO;
import com.steccn.platform.dto.SysOfficeTypeKeyDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareUnitConvertFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmsPullForce;
import com.steccn.pms.dto.ProducePassRateDTO;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.service.PmsPullForceService;
import com.steccn.pms.service.ProducePassRateService;
import com.steccn.pms.util.UnitConvertUtil;
import com.steccn.pms.vo.PassRateDateVo;
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
    private final RemoteSpareUnitConvertFeignApi remoteSpareUnitConvertFeignApi;


    @Override
    public List<ProducePassRateVO> getProducePassRateGroup(ProducePassRateDTO dto) {
        //获取日期
        List<LocalDate> localDateList = LocalDateUtil.getLocalDateByStartEndDate(dto.getBeginDate(), dto.getEndDate());

        //分组获取产量数据
        List<PmsReportWorkGroupVO> pmsReportWorkGroupVOList = getPmsReportWorkGroupVOList(localDateList, dto);


        QueryWrapper<PmsPullForce> queryWrapper = getQueryWrapper(dto);
        List<PmsPullForce> pmsPullForceList = pmsPullForceService.list(queryWrapper);
        //过滤数据
        if(CollUtil.isNotEmpty(dto.getFilters())&&dto.getCompare()!=null){
            //对比的是班次
            if(dto.getCompare().equals(PmManagerConstant.QtChartCompareEnum.WORK.getCode())){
                Set<String> className = Sets.newHashSet(dto.getFilters());
                Map<String, Long> classMap = FeignOptional.of(() -> remoteDictFeignApi.listByNameCh(className), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                List<Long> classIds = Lists.newArrayList(classMap.values());
                pmsReportWorkGroupVOList = pmsReportWorkGroupVOList.stream().filter(v -> classIds.contains(v.getClassesId())).collect(Collectors.toList());
                pmsPullForceList = pmsPullForceList.stream().filter(v -> classIds.contains(v.getClassesId())).collect(Collectors.toList());
            }
            //对比的是车间
            if(dto.getCompare().equals(PmManagerConstant.QtChartCompareEnum.WORKSHOP.getCode())){
                Set<String> officeNames = Sets.newHashSet(dto.getFilters());
                Map<String, SysOfficeVO> officeVOMap = FeignOptional.of(() -> remoteOfficeFeign.listByName(officeNames), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                //把所有id收集
                List<Integer> workshopIds = Lists.newArrayList();
                for(Map.Entry<String,SysOfficeVO> entry:officeVOMap.entrySet()){
                    workshopIds.add(entry.getValue().getId());
                }
                pmsReportWorkGroupVOList = pmsReportWorkGroupVOList.stream().filter(v -> workshopIds.contains(v.getWorkshopId())).collect(Collectors.toList());
                pmsPullForceList = pmsPullForceList.stream().filter(v -> workshopIds.contains(v.getWorkshopId())).collect(Collectors.toList());
            }
            //对比是产线
            if(dto.getCompare().equals(PmManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode())){
                Set<String> officeNames = Sets.newHashSet(dto.getFilters());
                Map<String, SysOfficeVO> officeVOMap = FeignOptional.of(() -> remoteOfficeFeign.listByName(officeNames), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                //把所有id收集
                List<Integer> productLineIds = Lists.newArrayList();
                for(Map.Entry<String,SysOfficeVO> entry:officeVOMap.entrySet()){
                    productLineIds.add(entry.getValue().getId());
                }
                pmsReportWorkGroupVOList = pmsReportWorkGroupVOList.stream().filter(v -> productLineIds.contains(v.getProductLineId())).collect(Collectors.toList());
                pmsPullForceList = pmsPullForceList.stream().filter(v -> productLineIds.contains(v.getProductLineId())).collect(Collectors.toList());
            }
        }
        localDateList.remove(localDateList.size() - 1);
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
            //获取每天的合格率
            BigDecimal dayPassRate = null;
            if (CollectionUtils.isNotEmpty(pmsPullForceList)) {
                Map<Integer, List<PmsReportWorkGroupVO>> groupBy = pmsReportWorkGroupVOList
                        .stream()
                        .collect(Collectors.groupingBy(PmsReportWorkGroupVO::getProductLineId));
                BigDecimal dayWorkSum = null;
                BigDecimal pullForceSum = null;
                //根据产线分组
                for (Map.Entry<Integer, List<PmsReportWorkGroupVO>> entry : groupBy.entrySet()) {
                    //获取每条产线的产量和拉引量
                    List<BigDecimal> dayWorkList = reportWorkList.stream().filter(reportWork -> Objects.equals(reportWork.getProductLineId(), entry.getKey())).map(PmsReportWorkGroupVO::getQuantity).collect(Collectors.toList());
                    BigDecimal dayWork = BigDecimalUtil.sum(dayWorkList);
                    List<BigDecimal> pullForceList = pmsPullForceList.stream().filter(pmsPullForce -> pmsPullForce.getPullForceDate().equals(localDate) && Objects.equals(entry.getKey(), pmsPullForce.getProductLineId())).map(PmsPullForce::getQuantity).collect(Collectors.toList());
                    BigDecimal pullForce = BigDecimalUtil.sum(pullForceList);
                    if (Objects.nonNull(dayWork) && Objects.nonNull(pullForce)) {
                        dayWorkSum = BigDecimalUtil.add(dayWorkSum, dayWork);
                        pullForceSum = BigDecimalUtil.add(pullForceSum, pullForce);
                    }
                }
                dayPassRate = BigDecimalUtil.divide(dayWorkSum, pullForceSum, 4);
            }

            //获取每个班次的合格率
            for (PmsReportWorkGroupVO reportWorkGroupVO : reportWorkList) {
                ProducePassRateVO producePassRateVO = new ProducePassRateVO();
                producePassRateVO.setDate(localDate);
                producePassRateVO.setClassesId(reportWorkGroupVO.getClassesId());
                producePassRateVO.setProductLineId(reportWorkGroupVO.getProductLineId());
                producePassRateVO.setWorkshopId(reportWorkGroupVO.getWorkshopId());
                producePassRateVO.setDayPassRate(dayPassRate);
                producePassRateVO.setYield(reportWorkGroupVO.getQuantity());

                if (CollectionUtils.isNotEmpty(pmsPullForceList)) {
                    Optional<PmsPullForce> optional = pmsPullForceList.stream()
                            .filter(pmsPullForce -> pmsPullForce.getPullForceDate().equals(reportWorkGroupVO.getGroupDate())
                                    && pmsPullForce.getClassesId().equals(reportWorkGroupVO.getClassesId())
                                    && Objects.nonNull(reportWorkGroupVO.getWorkshopId())
                                    && pmsPullForce.getWorkshopId().equals(reportWorkGroupVO.getWorkshopId())
                                    && pmsPullForce.getProductLineId().equals(reportWorkGroupVO.getProductLineId())).findAny();
                    if (optional.isPresent()) {
                        producePassRateVO.setLaAmount(optional.get().getQuantity());
                        producePassRateVO.setPassRate(BigDecimalUtil.divide(reportWorkGroupVO.getQuantity(), optional.get().getQuantity(), 4));
                    } else {
                        producePassRateVO.setPassRate(null);
                    }
                } else {
                    producePassRateVO.setPassRate(null);
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
            producePassRateVO.setWorkshopName(sysOfficeVoMap.getOrDefault(producePassRateVO.getWorkshopId(), defaultSysOfficeVO).getOfficeName());
            producePassRateVO.setProductLineName(sysOfficeVoMap.getOrDefault(producePassRateVO.getProductLineId(), defaultSysOfficeVO).getOfficeName());
        });

        return producePassRateVOList;
    }

    private QueryWrapper<PmsPullForce> getQueryWrapper(ProducePassRateDTO dto) {

        QueryWrapper<PmsPullForce> queryWrapper = new QueryWrapper<PmsPullForce>();
        queryWrapper.select("pull_force_date as pullForceDate,workshop_id as workshopId,product_line_id ,classes_id as classesId, sum(quantity) as quantity");

        List<SFunction<PmsPullForce, ?>> sFunctionList = new ArrayList<>();
        sFunctionList.add(PmsPullForce::getPullForceDate);
        sFunctionList.add(PmsPullForce::getWorkshopId);
        sFunctionList.add(PmsPullForce::getProductLineId);
        sFunctionList.add(PmsPullForce::getClassesId);

        queryWrapper.lambda()
                .ge(PmsPullForce::getPullForceDate, dto.getBeginDate())
                .le(PmsPullForce::getPullForceDate, dto.getEndDate()).groupBy(sFunctionList);
        if (CollectionUtils.isNotEmpty(dto.getProductLineIdList())) {
            queryWrapper.lambda().in(PmsPullForce::getProductLineId, dto.getProductLineIdList());
        }
        return queryWrapper;
    }

    private List<PmsReportWorkGroupVO> getPmsReportWorkGroupVOList(List<LocalDate> localDateList, ProducePassRateDTO dto) {
        LocalTime localTime = LocalTime.of(8, 0, 0);
        LocalDateTime beginDateTime = LocalDateTime.of(dto.getBeginDate(), localTime);
        LocalDateTime endDateTime = LocalDateTime.of(dto.getEndDate(), localTime);

        List<LocalDate> dateList = new ArrayList<>(localDateList);
        Map<LocalDate, PassRateDateVo> dateMap = Maps.newHashMap();
        while (!CollectionUtils.isEmpty(dateList)) {
            PassRateDateVo rateDateVo = new PassRateDateVo();
            LocalDate endDate = localDateList.get(dateList.size() - 1);
            LocalDate startDate = localDateList.get(dateList.size() - 2);

            rateDateVo.setStartDateTime(LocalDateTime.of(startDate, localTime));
            rateDateVo.setEndDateTime(LocalDateTime.of(endDate, localTime));
            dateList.remove(endDate);
            if (dateList.size() == 1) {
                dateList.remove(startDate);
            }
            dateMap.put(startDate, rateDateVo);
        }

        List<PmsReportWorkGroupVO> reportWorkGroupVOList = pmsReportWorkMapper.selectAllGroupByDate(beginDateTime, endDateTime, dto.getProductLineIdList());
        Set<Integer> childIdList = reportWorkGroupVOList.stream().map(PmsReportWorkGroupVO::getProductLineId).collect(Collectors.toSet());
        Map<Integer, SysOfficeVO> sysOfficeMap = remoteOfficeFeign.getParentOfficeMap(new ArrayList<>(childIdList)).getData();
        Set<Long> materialIdSet = reportWorkGroupVOList.stream().map(PmsReportWorkGroupVO::getMaterialId).collect(Collectors.toSet());
        Map<Long, List<SysSpareUnitConversionVO>> unitMap = remoteSpareUnitConvertFeignApi.convert(materialIdSet).getData();
        SysDictVO sysDictVO1 = remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, PmManagerConstant.MEASURE_UNIT).getData();
        SysDictVO sysDictVO2 = remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_TON, PmManagerConstant.MEASURE_UNIT).getData();
        Assert.notNull(sysDictVO1, "换算单位[片]不存在");
        Assert.notNull(sysDictVO2, "换算单位[吨]不存在");
        //获取分组日期
        for (PmsReportWorkGroupVO pmsReportWorkGroupVO : reportWorkGroupVOList) {
            LocalDateTime dateTime = pmsReportWorkGroupVO.getDate();
            for (Map.Entry<LocalDate, PassRateDateVo> entry : dateMap.entrySet()) {
                LocalDateTime startTime = entry.getValue().getStartDateTime();
                LocalDateTime endTime = entry.getValue().getEndDateTime();
                boolean bool = dateTime.isAfter(startTime) || dateTime.isEqual(startTime);
                if (bool && dateTime.isBefore(endTime)) {
                    pmsReportWorkGroupVO.setGroupDate(entry.getKey());
                }
            }
            //根据产线id获取车间id
            if (Objects.nonNull(sysOfficeMap)) {
                pmsReportWorkGroupVO.setWorkshopId(sysOfficeMap.get(pmsReportWorkGroupVO.getProductLineId()).getParentId());
            }
            //单位转换
            BigDecimal convertBd = UnitConvertUtil.convertMaterial(unitMap.get(pmsReportWorkGroupVO.getMaterialId()), sysDictVO1.getId(), sysDictVO2.getId(), pmsReportWorkGroupVO.getQuantity(), 2);
            pmsReportWorkGroupVO.setQuantity(Objects.isNull(convertBd) ? BigDecimal.ZERO : convertBd);
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
            //判断是否班次是否为null
            Long classesId = null;
            if(Objects.nonNull(splits[3]) && StrUtil.isNumeric(splits[3])){
                classesId = Long.parseLong(splits[3]);
            }
            List<BigDecimal> totalList = entry.getValue().stream().map(PmsReportWorkGroupVO::getQuantity).collect(Collectors.toList());
            BigDecimal total = BigDecimalUtil.sum(totalList);
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
