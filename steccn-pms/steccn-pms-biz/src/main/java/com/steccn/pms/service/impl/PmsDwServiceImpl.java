package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.data.dto.DwStationCollectDTO;
import com.steccn.data.feign.RemoteDataPlatformFeignApi;
import com.steccn.data.vo.DwStationCollectVO;
import com.steccn.platform.feign.RemoteSysOfficeCollectFeignApi;
import com.steccn.platform.vo.SysOfficeCollectVO;
import com.steccn.pms.service.PmsDwService;
import com.steccn.pms.service.PmsProcessInStationService;
import com.steccn.pms.service.PmsProcessOutStationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
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
            List<DwStationCollectVO> dwVOList = FeignOptional.of(remoteDataPlatformFeignApi.stationCollect(dwStationCollectDTO)).get();
            //3.解析数仓返回的报文
            log.info("dwVOList -> {}", dwVOList);
            
        }


        return null;
    }

    @Override
    public Boolean outStation(LocalDate date) {
        return null;
    }

}
