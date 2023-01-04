package com.steccn.qms.service.impl;

import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.service.QtCheckTaskService;
import com.steccn.qms.service.QtDayReportService;
import com.steccn.qms.service.QtDefectiveReportService;
import com.steccn.qms.vo.QtCheckTaskDetailReportVO;
import com.steccn.qms.vo.QtDayReportOnlineVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 质量日报表-接口实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QtDayReportServiceImpl implements QtDayReportService {

    private final QtDefectiveReportService qtDefectiveReportService;

    @Override
    public QtDayReportOnlineVO online(LocalDate date, Integer productLineId) {
        //查询人工数据

        //获取所有扁平化数据
        QtChartParamDTO dto = new QtChartParamDTO();
        dto.setDateStart(date);
        dto.setDateEnd(date);
        dto.setProductLineId(productLineId);
        List<QtCheckTaskDetailReportVO> manDataList = qtDefectiveReportService.defectiveDataFlat(dto);
        log.info("{}", manDataList);
        //查询检验设备数据


        return null;
    }
}
