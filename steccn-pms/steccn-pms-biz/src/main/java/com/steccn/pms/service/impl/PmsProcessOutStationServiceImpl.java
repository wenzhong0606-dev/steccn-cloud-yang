package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.api.config.filter.IFilterConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.data.dto.DwStationInfoDTO;
import com.steccn.data.feign.RemoteDataPlatformFeignApi;
import com.steccn.data.vo.DwStationCollectVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSysOfficeCollectFeignApi;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.pms.domain.PmsProcessOutStation;
import com.steccn.pms.domain.PmsWorkOrderBatch;
import com.steccn.pms.dto.PmsProcessOutStationPageDTO;
import com.steccn.pms.service.PmsProcessOutStationService;
import com.steccn.pms.mapper.PmsProcessOutStationMapper;
import com.steccn.pms.service.PmsWorkOrderBatchService;
import com.steccn.pms.vo.PmsProcessOutStationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_process_out_station(出站信息)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
@RequiredArgsConstructor
public class PmsProcessOutStationServiceImpl extends ServiceImpl<PmsProcessOutStationMapper, PmsProcessOutStation>
        implements PmsProcessOutStationService {


    private final RemoteSysOfficeCollectFeignApi remoteSysOfficeCollectFeignApi;

    private final RemoteDataPlatformFeignApi remoteDataPlatformFeignApi;

    private final PmsWorkOrderBatchService pmsWorkOrderBatchService;

    private final RemoteOfficeFeign remoteOfficeFeign;

    @Override
    public Page<PmsProcessOutStationVO> outPage(PmsProcessOutStationPageDTO dto) {
        List<PmsProcessOutStationVO> list = new ArrayList<>();
        //获取当前工序的执行工位
        Set<Integer> stationIds = new HashSet<>(dto.getStationIds());
        //获取工位绑定的采集点
        Map<Integer, List<String>> map = FeignOptional.of(() -> remoteSysOfficeCollectFeignApi.listByStation(stationIds, 1), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //获取订单的批次时间
        Map<Long, List<PmsWorkOrderBatch>> batchMap = pmsWorkOrderBatchService.listByOrderId(Sets.newHashSet(dto.getProduceOrderId()));
        List<PmsWorkOrderBatch> pmsWorkOrderBatches = batchMap.get(dto.getProduceOrderId());
        if (CollUtil.isNotEmpty(pmsWorkOrderBatches)) {
            List<DwStationCollectVO> dwStationCollectVOS = new ArrayList<>();
            for (PmsWorkOrderBatch pmsWorkOrderBatch : pmsWorkOrderBatches) {
                for (Integer stationId : stationIds) {
                    if (Objects.nonNull(map.get(stationId))) {
                        //获取采集点绑定的数据
                        DwStationInfoDTO dwStationInfoDTO = new DwStationInfoDTO();
                        dwStationInfoDTO.setCodeList(map.get(stationId));
                        dwStationInfoDTO.setStartTime(pmsWorkOrderBatch.getStartTime());
                        dwStationInfoDTO.setBatchNo(pmsWorkOrderBatch.getCode());
                        dwStationInfoDTO.setStationId(stationId);
                        if (Objects.isNull(pmsWorkOrderBatch.getEndTime())) {
                            //如果当前批次的结束时间为空，那么结束时间为当前时间
                            dwStationInfoDTO.setEndTime(LocalDateTime.now());
                        } else {
                            dwStationInfoDTO.setEndTime(pmsWorkOrderBatch.getEndTime());
                        }
                        dwStationCollectVOS.addAll(FeignOptional.of(() -> remoteDataPlatformFeignApi.stationInfo(dwStationInfoDTO), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get());
                    }
                }
            }
            if (CollUtil.isNotEmpty(dwStationCollectVOS)) {
                Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(stationIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                for (DwStationCollectVO dwStationCollectVO : dwStationCollectVOS) {
                    PmsProcessOutStationVO pmsProcessOutStationVO = new PmsProcessOutStationVO();
                    pmsProcessOutStationVO.setOutStationTime(dwStationCollectVO.getRecordTime());
                    pmsProcessOutStationVO.setQuantity(dwStationCollectVO.getValue());
                    pmsProcessOutStationVO.setBatchNo(dwStationCollectVO.getBatchNo());
                    pmsProcessOutStationVO.setStation(officeMap.get(dwStationCollectVO.getStationId()));
                    pmsProcessOutStationVO.setStationId(dwStationCollectVO.getStationId());
                    list.add(pmsProcessOutStationVO);
                }
            }
        }
        if (CollUtil.isNotEmpty(list)) {
            //条件过滤
            if (Objects.nonNull(dto.getStartTime()) && Objects.nonNull(dto.getEndTime())) {
                LocalDateTime startTime = LocalDateTime.of(dto.getStartTime(), LocalTime.MIN);
                LocalDateTime endTime = LocalDateTime.of(dto.getEndTime(), LocalTime.MAX);
                list = list.stream()
                        .filter(p -> LocalDateUtil.le(startTime, p.getOutStationTime()) && LocalDateUtil.ge(endTime, p.getOutStationTime()))
                        .collect(Collectors.toList());
                list = list.stream()
                        .filter(item -> dto.getBatchNo().contains(item.getBatchNo()))
                        .collect(Collectors.toList());
            }else {
                list.clear();
            }
        }
        return LogicalPageUtil.page(list, dto.getCurrent(), dto.getSize());
    }


}




