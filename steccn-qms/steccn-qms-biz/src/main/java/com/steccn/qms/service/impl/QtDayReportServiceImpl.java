package com.steccn.qms.service.impl;

import com.steccn.qms.service.QtDayReportService;
import com.steccn.qms.vo.QtDayReportOnlineVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * @author yequan
 * @desc 质量日报表-接口实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QtDayReportServiceImpl implements QtDayReportService {

    @Override
    public QtDayReportOnlineVO online(LocalDate date, Integer productLineId) {
        return null;
    }
}
