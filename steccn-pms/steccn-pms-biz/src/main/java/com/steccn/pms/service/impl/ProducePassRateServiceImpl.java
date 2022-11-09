package com.steccn.pms.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.pms.domain.PmsPullForce;
import com.steccn.pms.dto.ProducePassRateDTO;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.service.PmsProducePlanService;
import com.steccn.pms.service.PmsPullForceService;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.pms.service.ProducePassRateService;
import com.steccn.pms.vo.PmsReportWorkGroupVO;
import com.steccn.pms.vo.ProducePassRateVO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chenhuan
 * @date 2022/11/9 16:27
 */
@RequiredArgsConstructor
@Service
public class ProducePassRateServiceImpl implements ProducePassRateService {

    private final PmsReportWorkService pmsReportWorkService;

    private final PmsPullForceService pmsPullForceService;

    private final RemoteSpareFeignApi remoteSpareFeignApi;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final PmsReportWorkMapper pmsReportWorkMapper;


    @Override
    public List<ProducePassRateVO> getProducePassRateGroup(ProducePassRateDTO dto) {
        List<PmsReportWorkGroupVO> reportWorkGroupVOList = pmsReportWorkMapper.selectAllGroupByDate(
                dto.getProductionLineIdList()
                , dto.getBeginDate(), dto.getEndDate()
                ,dto.getGroupType());

        QueryWrapper<PmsPullForce> queryWrapper = new QueryWrapper<PmsPullForce>();
        queryWrapper.select("pull_force_date as date,classes_id as classesId, sum(quantity) as quantity");
        if (CollectionUtils.isNotEmpty(dto.getProductionLineIdList())) {
            queryWrapper.lambda().in(PmsPullForce::getProductLineId,dto.getProductionLineIdList());
        }
        if (CollectionUtils.isNotEmpty(dto.getClassIdList())) {
            queryWrapper.lambda().in(PmsPullForce::getClassesId,dto.getClassIdList());
        }
        List<SFunction<PmsPullForce,?>> sFunctionList = new ArrayList<>();
        sFunctionList.add(PmsPullForce::getPullForceDate);
        sFunctionList.add(PmsPullForce::getClassesId);
        queryWrapper.lambda()
                .ge(PmsPullForce::getPullForceDate,dto.getBeginDate())
                .le(PmsPullForce::getPullForceDate,dto.getEndDate()).groupBy(sFunctionList);

        List<PmsPullForce> pmsPullForceList = pmsPullForceService.list(queryWrapper);
        Map<LocalDate, List<PmsPullForce>> groupBy = pmsPullForceList.stream().collect(Collectors.groupingBy(PmsPullForce::getPullForceDate));

        return null;
    }
}
