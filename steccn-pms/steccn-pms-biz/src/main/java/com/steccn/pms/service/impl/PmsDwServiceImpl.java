package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.data.dto.DwStationCollectDTO;
import com.steccn.data.feign.RemoteDataPlatformFeignApi;
import com.steccn.data.vo.DwStationCollectVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteSysOfficeCollectFeignApi;
import com.steccn.platform.vo.SysOfficeCollectVO;
import com.steccn.pms.domain.PmsProcessInStation;
import com.steccn.pms.service.PmsDwService;
import com.steccn.pms.service.PmsProcessInStationService;
import com.steccn.pms.service.PmsProcessOutStationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 数仓接口实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PmsDwServiceImpl implements PmsDwService {

    private final PmsProcessInStationService pmsProcessInStationService;
    private final PmsProcessOutStationService pmsProcessOutStationService;
    private final RemoteSysOfficeCollectFeignApi remoteSysOfficeCollectFeignApi;
    private final RemoteDataPlatformFeignApi remoteDataPlatformFeignApi;
    private final RemoteDictFeignApi remoteDictFeignApi;

    @Override
    public Boolean inStation(LocalDate date) {
        //1.先查询所有的进站采集点编码配置
        List<SysOfficeCollectVO> collectList = FeignOptional.of(remoteSysOfficeCollectFeignApi.listOnlyStation(0)).get();
        //2.构造数仓
        if (CollUtil.isNotEmpty(collectList)) {
            DwStationCollectDTO dwStationCollectDTO = new DwStationCollectDTO();
            List<String> codeList = collectList.stream().map(SysOfficeCollectVO::getCode).collect(Collectors.toList());
            dwStationCollectDTO.setCodeList(codeList);
            dwStationCollectDTO.setDate(date);
            List<DwStationCollectVO> dwVOList = FeignOptional.of(() -> remoteDataPlatformFeignApi.stationCollect(dwStationCollectDTO), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get();
            //3.解析数仓返回的报文
            if (CollUtil.isNotEmpty(dwVOList)) {
                List<PmsProcessInStation> insertList = Lists.newArrayList();
                //获取工位对应的工序和批次
                //获取所有的时间，对应时间寻找工位
                Set<LocalDateTime> recordTimeSet = dwVOList.stream().map(DwStationCollectVO::getRecordTime).collect(Collectors.toSet());

                //遍历返回的结果
                for (DwStationCollectVO stationCollectVO : dwVOList) {
                    List<SysOfficeCollectVO> collect = collectList.stream().filter(v -> v.getCode().equals(stationCollectVO.getCode())).collect(Collectors.toList());
                    if (CollUtil.isNotEmpty(collect)) {
                        for (SysOfficeCollectVO co : collect) {
                            PmsProcessInStation model = new PmsProcessInStation();
                            model.setQuantity(stationCollectVO.getValue());
                            model.setStationId(co.getOfficeId());
                            model.setInStationTime(stationCollectVO.getRecordTime());
                            //单位为㎡
                            model.setUnitId(1551473814357700608L);
                            insertList.add(model);
                        }
                    }
                }
                if (CollUtil.isNotEmpty(insertList)) {
                    pmsProcessInStationService.saveBatch(insertList);
                }
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean outStation(LocalDate date) {
        return null;
    }

}
