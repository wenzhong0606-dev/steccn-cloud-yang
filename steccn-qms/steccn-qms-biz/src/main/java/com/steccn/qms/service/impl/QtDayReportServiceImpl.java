package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.dto.TupleDTO;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.service.QtDayReportService;
import com.steccn.qms.service.QtDefectiveReportService;
import com.steccn.qms.vo.QtCheckTaskDetailReportVO;
import com.steccn.qms.vo.QtDayReportOnlineVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 质量日报表-接口实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QtDayReportServiceImpl implements QtDayReportService {

    private final QtDefectiveReportService qtDefectiveReportService;
    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;


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
        Map<String, String> refMap = FeignOptional.of(remoteSysConfigurationFeignApi.sysConfRef(new TupleDTO<>(SysOfficeConstant.HBBH_DEFECTIVE_SAMPLE_LINE, Sets.newHashSet(productLineId)))).get();
        if (CollUtil.isNotEmpty(refMap)) {
            //获取到所有的映射产线
            Set<String> lineSet = refMap.values().stream().collect(Collectors.toSet());

        }
        return null;
    }
}
