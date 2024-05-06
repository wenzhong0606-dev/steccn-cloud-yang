package com.steccn.mes.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.collect.Lists;
import com.steccn.mes.domain.JsSysOffice;
import com.steccn.mes.domain.MesEqpClassification;
import com.steccn.mes.domain.MesEqpReport;
import com.steccn.mes.domain.MesEquipment;
import com.steccn.mes.dto.EqpOperationReportDTO;
import com.steccn.mes.service.MesEqpReportService;
import com.steccn.mes.mapper.MesEqpReportMapper;
import com.steccn.mes.vo.EqpOperationReportVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.vo.OaUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
* @author Admin
* @description 针对表【mes_eqp_report(设备运行报表)】的数据库操作Service实现
* @createDate 2024-05-06 16:36:23
*/
@Service
@RequiredArgsConstructor
public class MesEqpReportServiceImpl extends ServiceImpl<MesEqpReportMapper, MesEqpReport>
    implements MesEqpReportService{
    private final MesEqpReportMapper mesEqpReportMapper;

    @Override
    public List<EqpOperationReportVO> queryReport(EqpOperationReportDTO dto) {
        List<EqpOperationReportVO> results = mesEqpReportMapper.selectJoinList(EqpOperationReportVO.class,
                new MPJLambdaWrapper<EqpOperationReportVO>()
                        .selectAs(MesEquipment::getEquipmentCode, EqpOperationReportVO::getEqpCode)
                        .selectAs(MesEquipment::getEquipmentName, EqpOperationReportVO::getEqpName)
                        .selectAs(MesEquipment::getState, EqpOperationReportVO::getState)
                        .selectAs(MesEqpClassification::getName,EqpOperationReportVO::getClsName)
                        .selectAs(JsSysOffice::getOfficeName, EqpOperationReportVO::getOfficeName)
                        .selectAs(MesEqpReport::getStartPeriod, EqpOperationReportVO::getStartPeriod)
                        .selectAs(MesEqpReport::getStartPeriodAll,EqpOperationReportVO::getStartPeriodAll)
                        .selectAs(MesEqpReport::getStopPeriod,EqpOperationReportVO::getStopPeriod)
                        .selectAs(MesEqpReport::getMaxStartPeriod, EqpOperationReportVO::getMaxStartPeriod)
                        .selectAs(MesEqpReport::getMinStartPeriod, EqpOperationReportVO::getMinStartPeriod)
                        .leftJoin(MesEquipment.class, MesEquipment::getId, MesEqpReport::getEqpId)
                        .leftJoin(MesEquipment.class, MesEquipment::getClassification, MesEqpClassification::getId)
                        .leftJoin(MesEquipment.class, MesEquipment::getDepartment, JsSysOffice::getOfficeCode)
//                        .ne(Hrmresource::getStatus, OaConstant.NUM_5)
                        .like(StrUtil.isNotBlank(dto.getEqpCode()),MesEquipment::getEquipmentCode,dto.getEqpCode())
                        .like(StrUtil.isNotBlank(dto.getEqpName()),MesEquipment::getEquipmentName,dto.getEqpName())
                        .eq(StrUtil.isNotBlank(dto.getDepartmentCode()),MesEquipment::getDepartment,dto.getDepartmentCode())
                        .eq(StrUtil.isNotBlank(dto.getClsCode()),MesEquipment::getClassification,dto.getClsCode())
                        .eq(Objects.nonNull(dto.getState()),MesEquipment::getState,dto.getState())
                        .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
        if(CollUtil.isEmpty(results)){
            return Lists.newArrayList();
        }
        return results;
    }
}




