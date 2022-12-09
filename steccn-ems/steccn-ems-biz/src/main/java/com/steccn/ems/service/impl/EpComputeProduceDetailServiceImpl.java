package com.steccn.ems.service.impl;

import com.steccn.ems.service.EpComputeProduceDetailService;
import com.steccn.ems.vo.EpProduceDetailVO;
import com.steccn.pms.feign.RemoteReportWorkFeignApi;
import com.steccn.pms.vo.PmsReportWorkVO;
import com.steccn.qms.feign.RemoteCheckTaskDetailFeignApi;
import com.steccn.qms.vo.QtCheckTaskDetailItemMaterialVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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

        List<PmsReportWorkVO> reportWorkVOList = remoteReportWorkFeignApi.listByClassTime(startDateTime, endDateTime).getData();
        List<QtCheckTaskDetailItemMaterialVO> detailItemMaterialVOList = remoteCheckTaskDetailFeignApi.getByClassTime(startDateTime, endDateTime).getData();

        return null;
    }
}
