package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
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

import java.time.LocalDateTime;
import java.util.*;

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

    private final PmsWorkOrderBatchService  pmsWorkOrderBatchService;

    @Override
    public Page<PmsProcessOutStationVO> outPage(PmsProcessOutStationPageDTO dto) {
        List<PmsProcessOutStationVO> list =  new ArrayList<>();
        //获取当前工序的执行工位
        List<Integer> stationIds = dto.getStationIds();
        //获取工位绑定的采集点
        Map<Integer, String> map = FeignOptional.of(() -> remoteSysOfficeCollectFeignApi.listByStation((Set<Integer>) stationIds, 1), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<DwStationCollectVO> dwStationCollectVOS = new ArrayList<>();
        for (Integer stationId : stationIds) {
            if (Objects.nonNull(map.get(stationId))){
                //获取采集点绑定的数据
                DwStationInfoDTO dwStationInfoDTO = new DwStationInfoDTO();
                dwStationInfoDTO.setCode(map.get(stationId));
                dwStationCollectVOS.addAll(FeignOptional.of(() -> remoteDataPlatformFeignApi.stationInfo(dwStationInfoDTO), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get());
            }
        }
        if (CollUtil.isNotEmpty(dwStationCollectVOS)){
            for (DwStationCollectVO dwStationCollectVO : dwStationCollectVOS) {
                PmsProcessOutStationVO pmsProcessOutStationVO = new PmsProcessOutStationVO();
                pmsProcessOutStationVO.setOutStationTime(dwStationCollectVO.getRecordTime());
                pmsProcessOutStationVO.setQuantity(dwStationCollectVO.getValue());
                pmsProcessOutStationVO.setBatchNo(getBatch(dwStationCollectVO.getRecordTime(),dto.getProduceOrderId()));
                list.add(pmsProcessOutStationVO);
            }
        }
        return LogicalPageUtil.page(list, dto.getCurrent(), dto.getSize());
    }

    /**
     * 根据时间获取当前订单的所属批次
     * @param dateTime
     * @return
     */
    private String getBatch(LocalDateTime dateTime,Long produceOrderId){
        Map<Long, List<PmsWorkOrderBatch>> batchMap = pmsWorkOrderBatchService.listByOrderId(Sets.newHashSet(produceOrderId));
        List<PmsWorkOrderBatch> pmsWorkOrderBatches = batchMap.get(produceOrderId);
        if (CollUtil.isNotEmpty(pmsWorkOrderBatches)){
            return null;
        }
        for (PmsWorkOrderBatch pmsWorkOrderBatch : pmsWorkOrderBatches) {
            if (LocalDateUtil.le(pmsWorkOrderBatch.getStartTime(),dateTime) && LocalDateUtil.ge(pmsWorkOrderBatch.getEndTime(),dateTime)){
                return pmsWorkOrderBatch.getCode();
            }
        }
        return null;
    }
}




