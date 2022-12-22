package com.steccn.pms.service.impl;

import com.steccn.authserver.util.FeignOptional;
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

    @Override
    public Boolean inStation(LocalDate date) {
        //1.先查询所有的进站采集点编码配置
        List<SysOfficeCollectVO> collectList = FeignOptional.of(remoteSysOfficeCollectFeignApi.listOnlyStation(0)).get();
        log.info("collectList -> {}", collectList);


        return null;
    }

    @Override
    public Boolean outStation(LocalDate date) {
        return null;
    }

}
