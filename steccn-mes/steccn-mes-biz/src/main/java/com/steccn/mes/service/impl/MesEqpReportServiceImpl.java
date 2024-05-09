package com.steccn.mes.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.collect.Lists;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.mes.domain.JsSysOffice;
import com.steccn.mes.domain.MesEqpClassification;
import com.steccn.mes.domain.MesEqpReport;
import com.steccn.mes.domain.MesEquipment;
import com.steccn.mes.dto.EqpOperationReportDTO;
import com.steccn.mes.dto.EqpReportDTO;
import com.steccn.mes.mapper.MesEquipmentMapper;
import com.steccn.mes.service.MesEqpReportService;
import com.steccn.mes.mapper.MesEqpReportMapper;
import com.steccn.mes.vo.EqpOperationReportVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.vo.OaUserVO;
import com.steccn.qms.vo.QtCustomerComplainVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
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
    private final MesEquipmentMapper mesEquipmentMapper;

    @Override
    public Page<EqpOperationReportVO> queryReport(EqpOperationReportDTO dto) {
        //1.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //2.查询分页数据
        IPage<EqpOperationReportVO> resPage = mesEqpReportMapper.selectJoinPage(page, EqpOperationReportVO.class, new MPJLambdaWrapper<EqpOperationReportVO>()
                .selectAs(MesEquipment::getEquipmentCode, EqpOperationReportVO::getEqpCode)
                .selectAs(MesEquipment::getId, EqpOperationReportVO::getEqpId)
                .selectAs(MesEquipment::getEquipmentName, EqpOperationReportVO::getEqpName)
                .selectAs(MesEquipment::getState, EqpOperationReportVO::getState)
                .selectAs(MesEqpClassification::getName,EqpOperationReportVO::getClsName)
                .selectAs(JsSysOffice::getOfficeName, EqpOperationReportVO::getOfficeName)
                .selectAs(MesEqpReport::getStartTime, EqpOperationReportVO::getStartTime)
                .selectAs(MesEqpReport::getStartPeriodAll,EqpOperationReportVO::getStartPeriodAll)
                .selectAs(MesEqpReport::getStopPeriod,EqpOperationReportVO::getStopPeriod)
                .selectAs(MesEqpReport::getMaxStartPeriod, EqpOperationReportVO::getMaxStartPeriod)
                .selectAs(MesEqpReport::getMinStartPeriod, EqpOperationReportVO::getMinStartPeriod)
                .leftJoin(MesEquipment.class, MesEquipment::getId, MesEqpReport::getEqpId)
                .leftJoin(MesEqpClassification.class, MesEqpClassification::getId,MesEquipment::getClassification)
                .leftJoin(JsSysOffice.class, JsSysOffice::getOfficeCode,MesEquipment::getDepartment)
                .like(StrUtil.isNotBlank(dto.getEqpCode()),MesEquipment::getEquipmentCode,dto.getEqpCode())
                .like(StrUtil.isNotBlank(dto.getEqpName()),MesEquipment::getEquipmentName,dto.getEqpName())
                .eq(StrUtil.isNotBlank(dto.getDepartmentCode()),MesEquipment::getDepartment,dto.getDepartmentCode())
                .eq(StrUtil.isNotBlank(dto.getClsCode()),MesEquipment::getClassification,dto.getClsCode())
                .in(CollUtil.isNotEmpty(dto.getState()),MesEquipment::getState,dto.getState())
                .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
        //处理运行时长
        if(CollUtil.isNotEmpty(resPage.getRecords())){
            resPage.getRecords().forEach(tmp->{
                if(tmp.getState().equals(OaConstant.NO)&&Objects.nonNull(tmp.getStartTime())){
                    Duration duration2 = Duration.between(tmp.getStartTime(), LocalDateTime.now());
                    BigDecimal hours2 = BigDecimal.valueOf(duration2.getSeconds()).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP).divide(BigDecimal.valueOf(60));
                    tmp.setStartPeriod(hours2);
                }
            });
        }
        //3.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        return page;
    }

    @Override
    public Boolean changeReportData(EqpReportDTO dto) {
        //先根据code查询设备信息
        MesEquipment equipment = mesEquipmentMapper.selectOne(new LambdaQueryWrapper<MesEquipment>().eq(MesEquipment::getEquipmentCode, dto.getEqpCode())
                .eq(MesEquipment::getStatus, OaConstant.NO.toString()).last("LIMIT 1"));
        //先查看是否有数据
        MesEqpReport report = getOne(new LambdaQueryWrapper<MesEqpReport>().eq(MesEqpReport::getEqpId, equipment.getId()).last("LIMIT 1"));
        if(Objects.isNull(report)){
            //如果为空则加入数据
            MesEqpReport model = new MesEqpReport();
            model.setEqpId(equipment.getId().toString());
            if(dto.getState().equals(OaConstant.NO)){
                //0表示停机状态，则value是启动时长
                model.setMaxStartPeriod(dto.getValue());
                model.setMinStartPeriod(dto.getValue());
                model.setStartPeriodAll(dto.getValue());
            }else {
                //1表示开机状态，则value是停止时长
                model.setStartTime(dto.getTimestamp());
                model.setStopPeriod(dto.getValue());
            }
            save(model);
        }else {
            //否则修改数据
            MesEqpReport model = new MesEqpReport();
            model.setId(report.getId());
            if(dto.getState().equals(OaConstant.NO)){
                //0表示停机状态，则value是启动时长
                if(Objects.isNull(report.getStartPeriodAll())){
                    model.setStartPeriodAll(dto.getValue());
                }else {
                    model.setStartPeriodAll(BigDecimalUtil.add(report.getStartPeriodAll(),dto.getValue()));
                }
                if(Objects.isNull(report.getMaxStartPeriod())){
                    model.setMaxStartPeriod(dto.getValue());
                }else {
                    model.setMaxStartPeriod(dto.getValue().compareTo(report.getMaxStartPeriod())==1?dto.getValue():report.getMaxStartPeriod());
                }
                if(Objects.isNull(report.getMinStartPeriod())){
                    model.setMinStartPeriod(dto.getValue());
                }else {
                    model.setMinStartPeriod(dto.getValue().compareTo(report.getMinStartPeriod())==-1?dto.getValue():report.getMinStartPeriod());
                }

            }else {
                //1表示开 机状态，则value是停止时长
                model.setStopPeriod(Objects.isNull(report.getStopPeriod())?dto.getValue():BigDecimalUtil.add(report.getStopPeriod(),dto.getValue()));
                model.setStartTime(dto.getTimestamp());
            }
            updateById(model);
        }
        return true;
    }
}




