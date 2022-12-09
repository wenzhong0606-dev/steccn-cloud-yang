package com.steccn.ems.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.ems.service.EpComputeProduceDetailService;
import com.steccn.ems.vo.EpProduceDetailVO;
import com.steccn.pms.feign.RemoteReportWorkFeignApi;
import com.steccn.pms.vo.PmsReportWorkVO;
import com.steccn.qms.feign.RemoteCheckTaskDetailFeignApi;
import com.steccn.qms.vo.QtCheckTaskDetailItemMaterialVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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


    @Override
    public List<EpProduceDetailVO> getProduceDetailList(LocalDate startDate, LocalDate endDate) {
        LocalTime localTime = LocalTime.of(8,0,0);
        LocalDateTime startDateTime = LocalDateTime.of(startDate,localTime);
        LocalDateTime endDateTime = LocalDateTime.of(endDate,localTime);
        List<LocalDate> localDateList = LocalDateUtil.getLocalDateByStartEndDate(startDate, endDate);

        List<PmsReportWorkVO> reportWorkVOList = remoteReportWorkFeignApi.listByClassTime(startDateTime, endDateTime).getData();
        List<QtCheckTaskDetailItemMaterialVO> detailItemMaterialVOList = remoteCheckTaskDetailFeignApi.getByClassTime(startDateTime, endDateTime).getData();

        for (LocalDate localDate : localDateList) {
            LocalDateTime startTime = LocalDateTime.of(localDate,localTime);
            LocalDateTime endTime = LocalDateTime.of(localDate.plusDays(1),localTime);


            List<PmsReportWorkVO> pmsReportWorkVOList = getDayPmsReportWorkVOList(reportWorkVOList,startTime,endTime);

            //获取某天白班的巡检任务
            List<QtCheckTaskDetailItemMaterialVO> itemMaterialVOList = detailItemMaterialVOList.stream()
                    .filter(detailItemMaterial -> detailItemMaterial.getClassTime().isAfter(startTime) && detailItemMaterial.getClassTime().isBefore(endTime) && detailItemMaterial.getClassId().equals(0))
                    .collect(Collectors.toList());


            for (PmsReportWorkVO pmsReportWorkVO : pmsReportWorkVOList) {
                EpProduceDetailVO epProduceDetailVO = new EpProduceDetailVO();
                epProduceDetailVO.setClassDate(localDate);
                epProduceDetailVO.setClassId(0);
                epProduceDetailVO.setOfficeId(pmsReportWorkVO.getStationId());
//                epProduceDetailVO.setThickQuantity();
                List<QtCheckTaskDetailItemMaterialVO> taskDetailItemMaterialVOList = itemMaterialVOList.stream().filter(itemMaterial -> itemMaterial.getMaterialId().equals(pmsReportWorkVO.getMaterialId())).collect(Collectors.toList());
                if (pmsReportWorkVO.getWorkshopName().contains("基片")) {
                    BigDecimal pullLeadQuantity = getPullLeadQuantity(taskDetailItemMaterialVOList);
                } else if (pmsReportWorkVO.getWorkshopName().contains("深加工")) {

                }
            }

        }

        return null;
    }

    private List<PmsReportWorkVO> getDayPmsReportWorkVOList(List<PmsReportWorkVO> reportWorkVOList,LocalDateTime startTime, LocalDateTime endTime) {
        //获取某天白班的报工信息
        List<PmsReportWorkVO> pmsReportWorkVOList = reportWorkVOList.stream()
                .filter(reportWorkVO -> reportWorkVO.getStartTime().isAfter(startTime) && reportWorkVO.getEndTime().isBefore(endTime) && reportWorkVO.getClassesId().equals(1L))
                .collect(Collectors.toList());
        Map<Long, List<PmsReportWorkVO>> groupBy = pmsReportWorkVOList.stream().collect(Collectors.groupingBy(PmsReportWorkVO::getMaterialId));

        List<PmsReportWorkVO> pmsReportWorkVOList1 = new ArrayList<>();
        for (Map.Entry<Long, List<PmsReportWorkVO>> entry : groupBy.entrySet()) {
            PmsReportWorkVO pmsReportWorkVO = new PmsReportWorkVO();
            BeanUtils.copyProperties(entry.getValue().get(0),pmsReportWorkVO);
            BigDecimal sum = entry.getValue().stream().map(PmsReportWorkVO::getBadnessQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
            pmsReportWorkVO.setProcessQuantity(sum);
            pmsReportWorkVOList1.add(pmsReportWorkVO);
        }
        return pmsReportWorkVOList1;
    }

    private BigDecimal getPullLeadQuantity(List<QtCheckTaskDetailItemMaterialVO> itemMaterialVOList) {
        //获取每个物料的平均单片掰边重量、平均单片重量、拉引速度
        List<QtCheckTaskDetailItemMaterialVO> snappingList = itemMaterialVOList.stream().filter(item -> item.getItemName().equals("单片掰边重量")).collect(Collectors.toList());
        List<QtCheckTaskDetailItemMaterialVO> singleWeightList = itemMaterialVOList.stream().filter(item -> item.getItemName().equals("单片重量")).collect(Collectors.toList());
        List<QtCheckTaskDetailItemMaterialVO> pullSpeedList = itemMaterialVOList.stream().filter(item -> item.getItemName().equals("拉引速度")).collect(Collectors.toList());
        BigDecimal sumSnapping = snappingList.stream().map(item -> new BigDecimal(item.getCheckResult())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumSingleWeight = singleWeightList.stream().map(item -> new BigDecimal(item.getCheckResult())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sumPullSpeed = pullSpeedList.stream().map(item -> new BigDecimal(item.getCheckResult())).reduce(BigDecimal.ZERO, BigDecimal::add);
        //平均单片掰边重量
        BigDecimal avgSnapping = BigDecimalUtil.divide(sumSnapping,BigDecimal.valueOf(snappingList.size()));
        //平均单片重量
        BigDecimal avgSingleWeight = BigDecimalUtil.divide(sumSingleWeight,BigDecimal.valueOf(snappingList.size()));
        //拉引速度
        BigDecimal avgPullSpeed = BigDecimalUtil.divide(sumPullSpeed,BigDecimal.valueOf(snappingList.size()));
        BigDecimal glassLength = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(itemMaterialVOList)) {
            glassLength = itemMaterialVOList.get(0).getLengthQuantity();
        }
        //当前班次每个物料的拉引量
//        BigDecimal pullLeadQuantity = avgPullSpeed.add(avgSingleWeight).multiply(BigDecimalUtil.divide(BigDecimalUtil.divide(avgPullSpeed, glassLength), BigDecimal.valueOf(1000000)));
        return avgPullSpeed.add(avgSingleWeight).multiply(BigDecimalUtil.divide(BigDecimalUtil.divide(avgPullSpeed, glassLength), BigDecimal.valueOf(1000000)));
    }
}
