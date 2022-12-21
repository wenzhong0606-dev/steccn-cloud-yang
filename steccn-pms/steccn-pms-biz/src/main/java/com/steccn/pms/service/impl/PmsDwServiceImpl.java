package com.steccn.pms.service.impl;

import com.steccn.pms.service.PmsDwService;
import com.steccn.pms.service.PmsProcessInStationService;
import com.steccn.pms.service.PmsProcessOutStationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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

    @Override
    public Boolean inStation(LocalDate date) {
        return null;
    }

    @Override
    public Boolean outStation(LocalDate date) {
        return null;
    }
}
