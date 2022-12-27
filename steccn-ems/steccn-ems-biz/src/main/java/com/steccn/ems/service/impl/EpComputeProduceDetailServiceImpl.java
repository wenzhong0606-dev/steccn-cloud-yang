package com.steccn.ems.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.R;
import com.steccn.ems.service.EpComputeProduceDetailService;
import com.steccn.ems.vo.EpProduceDetailVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.feign.RemoteSpareUnitConvertFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import com.steccn.pms.feign.RemoteReportWorkFeignApi;
import com.steccn.pms.vo.PmsReportWorkVO;
import com.steccn.qms.feign.RemoteCheckTaskDetailFeignApi;
import com.steccn.qms.vo.QtCheckTaskDetailItemMaterialVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenhuan
 * @date 2022/12/9 14:29
 */
@Service
@RequiredArgsConstructor
public class EpComputeProduceDetailServiceImpl implements EpComputeProduceDetailService {

    private final RemoteReportWorkFeignApi remoteReportWorkFeignApi;

    private final RemoteCheckTaskDetailFeignApi remoteCheckTaskDetailFeignApi;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final RemoteSpareUnitConvertFeignApi remoteSpareUnitConvertFeignApi;

    private final RemoteSpareFeignApi remoteSpareFeignApi;


    @Override
    public List<EpProduceDetailVO> getProduceDetailList(LocalDate startDate, LocalDate endDate) {
        LocalTime localTime = LocalTime.of(8, 0, 0);
        LocalDateTime startDateTime = LocalDateTime.of(startDate, localTime);
        LocalDateTime endDateTime = LocalDateTime.of(endDate, localTime).plusDays(1);
        List<LocalDate> localDateList = LocalDateUtil.getLocalDateByStartEndDate(startDate, endDate);
        R<List<PmsReportWorkVO>> listR = remoteReportWorkFeignApi.listByClassTime(new TupleDTO<>(startDateTime, endDateTime));

        List<PmsReportWorkVO> reportWorkVOList = FeignOptional.of(listR).get();

        List<QtCheckTaskDetailItemMaterialVO> detailItemMaterialVOList = remoteCheckTaskDetailFeignApi.getByClassTime(new TupleDTO<>(startDateTime, endDateTime)).getData();
        List<SysDictVO> sysDictVOList = remoteDictFeignApi.listByType("sys_shift").getData();
        SysDictVO unit = remoteDictFeignApi.getByCode("m²", "measure_unit").getData();
        Set<Long> materialIdSet = reportWorkVOList.stream().map(PmsReportWorkVO::getMaterialId).collect(Collectors.toSet());
        Map<Long, List<SysSpareUnitConversionVO>> spareUnitMap = remoteSpareUnitConvertFeignApi.convert(materialIdSet).getData();
        Map<Long, EqpSpareVO> spareMap = remoteSpareFeignApi.listById(new ArrayList<>(materialIdSet)).getData();

        Long dayClassId = null;
        Long nightClassId = null;
        for (SysDictVO sysDictVO : sysDictVOList) {
            if (Objects.equals(sysDictVO.getCode(), "0")) {
                dayClassId = sysDictVO.getId();
            } else {
                nightClassId = sysDictVO.getId();
            }
        }

        List<EpProduceDetailVO> epProduceDetailVoList = new ArrayList<>();
        for (LocalDate localDate : localDateList) {
            LocalDateTime startTime = LocalDateTime.of(localDate, localTime);
            LocalDateTime endTime = LocalDateTime.of(localDate.plusDays(1), localTime);

            for (int i = 0; i < 2; i++) {
                List<PmsReportWorkVO> pmsReportWorkVOList;
                List<QtCheckTaskDetailItemMaterialVO> itemMaterialVOList;
                //获取某天某个班次的报工数
                if (Objects.equals(i, 0)) {
                    pmsReportWorkVOList = getDayPmsReportWorkVOList(reportWorkVOList, startTime, endTime, dayClassId);
                } else {
                    pmsReportWorkVOList = getDayPmsReportWorkVOList(reportWorkVOList, startTime, endTime, nightClassId);
                }
                //获取某天某个班次的巡检任务
                itemMaterialVOList = detailItemMaterialVOList.stream()
                        .filter(detailItemMaterial -> detailItemMaterial.getClassTime().isAfter(startTime) && detailItemMaterial.getClassTime().isBefore(endTime) && detailItemMaterial.getClassId().equals(1))
                        .collect(Collectors.toList());

                //获取某天某个班次某个物料的基片拉引量和深加工产量
                for (PmsReportWorkVO pmsReportWorkVO : pmsReportWorkVOList) {
                    EpProduceDetailVO epProduceDetailVO = new EpProduceDetailVO();
                    epProduceDetailVO.setClassDate(localDate);
                    epProduceDetailVO.setClassId(i);
                    epProduceDetailVO.setOfficeId(pmsReportWorkVO.getStationId());
                    epProduceDetailVO.setProductLineId(pmsReportWorkVO.getProductLineId());
                    List<QtCheckTaskDetailItemMaterialVO> taskDetailItemMaterialVOList = itemMaterialVOList.stream()
                            .filter(itemMaterial -> itemMaterial.getMaterialId().equals(pmsReportWorkVO.getMaterialId())
                            && itemMaterial.getProductLine().getId().equals(pmsReportWorkVO.getProductLineId())).collect(Collectors.toList());
                    EqpSpareVO spareVO = spareMap.get(pmsReportWorkVO.getMaterialId());
                    if (Objects.nonNull(spareVO)) {
                        epProduceDetailVO.setThickQuantity(spareVO.getThickQuantity());
                    }
                    if (pmsReportWorkVO.getWorkshopName().contains("基片")) {
                        BigDecimal pullLeadQuantity = getPullLeadQuantity(taskDetailItemMaterialVOList, pmsReportWorkVO.getDuration(), spareVO.getLengthQuantity());
                        epProduceDetailVO.setYield(pullLeadQuantity);
                    } else if (pmsReportWorkVO.getWorkshopName().contains("深加工")) {
                        //是否配了单位转换
                        if (CollectionUtils.isNotEmpty(spareUnitMap.get(pmsReportWorkVO.getMaterialId()))) {
                            Optional<SysSpareUnitConversionVO> optional = spareUnitMap.get(pmsReportWorkVO.getMaterialId())
                                    .stream().filter(sysSpareUnitConversionVO -> unit.getId().equals(sysSpareUnitConversionVO.getConversionUnitId())).findAny();
                            if (optional.isPresent()) {
                                BigDecimal conversionQuantity = optional.get().getConversionQuantity();
                                BigDecimal quantity = optional.get().getQuantity();
                                //进行单位转换
                                epProduceDetailVO.setYield(BigDecimalUtil.divide(pmsReportWorkVO.getProcessQuantity(), quantity).multiply(conversionQuantity));
                            }
                        }
                    }
                    epProduceDetailVoList.add(epProduceDetailVO);
                }
            }
        }

        List<EpProduceDetailVO> parentProduceDetailVOList = new ArrayList<>();
        Set<Integer> officeIdSet = epProduceDetailVoList.stream().map(EpProduceDetailVO::getOfficeId).collect(Collectors.toSet());
        Map<Integer, SysOfficeVO> parentOfficeMap = remoteOfficeFeign.getParentOfficeMap(new ArrayList<>(officeIdSet)).getData();
        Map<String, List<EpProduceDetailVO>> map = epProduceDetailVoList.stream().collect(Collectors.groupingBy(epProduceDetailVO -> epProduceDetailVO.getClassId()
                + "," + epProduceDetailVO.getClassDate()
                + "," + epProduceDetailVO.getMaterialId()
                + "," + epProduceDetailVO.getProductLineId()));
        for (Map.Entry<String, List<EpProduceDetailVO>> entry : map.entrySet()) {
            EpProduceDetailVO epProduceDetailVO = new EpProduceDetailVO();
            BeanUtils.copyProperties(entry.getValue().get(0),epProduceDetailVO);
            if (!parentOfficeMap.isEmpty()) {
                epProduceDetailVO.setOfficeId(parentOfficeMap.get(entry.getValue().get(0).getOfficeId()).getParentId());
            }
            parentProduceDetailVOList.add(epProduceDetailVO);
        }
        epProduceDetailVoList.addAll(parentProduceDetailVOList);
        recursionParentData(parentProduceDetailVOList, epProduceDetailVoList);

        return epProduceDetailVoList;
    }

    private void recursionParentData(List<EpProduceDetailVO> childProduceDetailVOList, List<EpProduceDetailVO> produceDetailVOList) {
        Set<Integer> officeIdSet = childProduceDetailVOList.stream().map(EpProduceDetailVO::getOfficeId).collect(Collectors.toSet());
        Map<Integer, SysOfficeVO> parentOfficeMap = remoteOfficeFeign.getParentOfficeMap(new ArrayList<>(officeIdSet)).getData();
        if (Objects.isNull(parentOfficeMap) || parentOfficeMap.isEmpty()) {
            return;
        }
        List<EpProduceDetailVO> parentProduceDetailVOList = new ArrayList<>();
        //把字节的改成父节点
        for (EpProduceDetailVO epProduceDetailVO : childProduceDetailVOList) {
            EpProduceDetailVO produceDetailVO = new EpProduceDetailVO();
            BeanUtils.copyProperties(epProduceDetailVO, produceDetailVO);
            produceDetailVO.setOfficeId(parentOfficeMap.get(epProduceDetailVO.getOfficeId()).getParentId());
            parentProduceDetailVOList.add(produceDetailVO);
        }
        //汇总父节点数据
        List<EpProduceDetailVO> sumParentProduceDetailList = new ArrayList<>();
        Map<String, List<EpProduceDetailVO>> map = parentProduceDetailVOList.stream().collect(Collectors.groupingBy(epProduceDetailVO -> epProduceDetailVO.getOfficeId()
                + "," + epProduceDetailVO.getClassId()
                + "," + epProduceDetailVO.getClassDate()
                + "," + epProduceDetailVO.getThickQuantity()));
        for (Map.Entry<String, List<EpProduceDetailVO>> entry : map.entrySet()) {
            EpProduceDetailVO parentProduceDetailVO = new EpProduceDetailVO();
            BeanUtils.copyProperties(entry.getValue().get(0), parentProduceDetailVO);
            BigDecimal sumYield = entry.getValue().stream().filter(parentProduceDetail -> Objects.nonNull(parentProduceDetail.getYield())).map(EpProduceDetailVO::getYield).reduce(BigDecimal.ZERO, BigDecimal::add);
            parentProduceDetailVO.setYield(sumYield);
            sumParentProduceDetailList.add(parentProduceDetailVO);
        }
        produceDetailVOList.addAll(sumParentProduceDetailList);
        //继续递归父节点数据
        recursionParentData(sumParentProduceDetailList, produceDetailVOList);
    }

    private List<PmsReportWorkVO> getDayPmsReportWorkVOList(List<PmsReportWorkVO> reportWorkVOList, LocalDateTime startTime, LocalDateTime endTime, Long classId) {
        //获取某天白班的报工信息
        List<PmsReportWorkVO> pmsReportWorkVOList = reportWorkVOList.stream()
                .filter(reportWorkVO -> reportWorkVO.getStartTime().isAfter(startTime) && reportWorkVO.getEndTime().isBefore(endTime) && reportWorkVO.getClassesId().equals(classId))
                .collect(Collectors.toList());
        Map<String, List<PmsReportWorkVO>> groupBy = pmsReportWorkVOList.stream().collect(Collectors.groupingBy(reportWorkVO -> reportWorkVO.getMaterialId() + "," + reportWorkVO.getStationId()));

        List<PmsReportWorkVO> reportWorkVoArrayList = new ArrayList<>();
        for (Map.Entry<String, List<PmsReportWorkVO>> entry : groupBy.entrySet()) {
            PmsReportWorkVO pmsReportWorkVO = new PmsReportWorkVO();
            BeanUtils.copyProperties(entry.getValue().get(0), pmsReportWorkVO);
            BigDecimal sum = entry.getValue().stream().map(PmsReportWorkVO::getBadnessQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            pmsReportWorkVO.setProcessQuantity(sum);

            List<BigDecimal> hoursList = new ArrayList<>();
            for (PmsReportWorkVO reportWorkVO : entry.getValue()) {
                LocalDateTime startTime1 = reportWorkVO.getStartTime();
                LocalDateTime endTime1 = reportWorkVO.getEndTime();
                Duration duration = Duration.between(startTime1, endTime1);
                long minutes = duration.toMinutes();
                //获取生产多少小时
                hoursList.add(BigDecimalUtil.divide(BigDecimal.valueOf(minutes), BigDecimal.valueOf(60)));
            }
            pmsReportWorkVO.setDuration(hoursList.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
            reportWorkVoArrayList.add(pmsReportWorkVO);
        }
        return reportWorkVoArrayList;
    }

    private BigDecimal getPullLeadQuantity(List<QtCheckTaskDetailItemMaterialVO> itemMaterialVOList, BigDecimal hours, BigDecimal glassLength) {
        //获取每个物料的平均单片掰边重量、平均单片重量、拉引速度
        List<QtCheckTaskDetailItemMaterialVO> snappingList = itemMaterialVOList.stream().filter(item -> item.getItemName().equals("单片掰边重量")).collect(Collectors.toList());
        List<QtCheckTaskDetailItemMaterialVO> singleWeightList = itemMaterialVOList.stream().filter(item -> item.getItemName().equals("单片重量")).collect(Collectors.toList());
        List<QtCheckTaskDetailItemMaterialVO> pullSpeedList = itemMaterialVOList.stream().filter(item -> item.getItemName().equals("拉引速度")).collect(Collectors.toList());
        BigDecimal sumSnapping = snappingList.stream().map(item -> new BigDecimal(item.getCheckResult())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumSingleWeight = singleWeightList.stream().map(item -> new BigDecimal(item.getCheckResult())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumPullSpeed = pullSpeedList.stream().map(item -> new BigDecimal(item.getCheckResult())).reduce(BigDecimal.ZERO, BigDecimal::add);
        //平均单片掰边重量
        BigDecimal avgSnapping = BigDecimalUtil.divide(sumSnapping, BigDecimal.valueOf(snappingList.size()));
        //平均单片重量
        BigDecimal avgSingleWeight = BigDecimalUtil.divide(sumSingleWeight, BigDecimal.valueOf(snappingList.size()));
        //拉引速度
        BigDecimal avgPullSpeed = BigDecimalUtil.divide(sumPullSpeed, BigDecimal.valueOf(snappingList.size()));
        //当前班次每个物料的拉引量
//        BigDecimal pullLeadQuantity = avgPullSpeed.add(avgSingleWeight).multiply(BigDecimalUtil.divide(BigDecimalUtil.divide(avgPullSpeed, glassLength), BigDecimal.valueOf(1000000)));
        return avgSnapping.add(avgSingleWeight).multiply(BigDecimalUtil.divide(BigDecimalUtil.divide(avgPullSpeed, glassLength).multiply(hours), BigDecimal.valueOf(1000000)));
    }
}
