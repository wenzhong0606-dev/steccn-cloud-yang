package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.pms.domain.PmsReportWorkHour;
import com.steccn.pms.dto.ProcessReportWorkHourDTO;
import com.steccn.pms.service.PmsReportWorkHourService;
import com.steccn.pms.mapper.PmsReportWorkHourMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * @author yequan
 * @description 针对表【pms_report_work_hour(报工工时信息)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
public class PmsReportWorkHourServiceImpl extends ServiceImpl<PmsReportWorkHourMapper, PmsReportWorkHour>
        implements PmsReportWorkHourService {

    @Override
    public void saveHourList(Long pmsReportWorkId, List<ProcessReportWorkHourDTO> hourDTOList) {
        if (CollUtil.isNotEmpty(hourDTOList)) {
            List<PmsReportWorkHour> hourModelList = Lists.newArrayList();
            for (ProcessReportWorkHourDTO hourDTO : hourDTOList) {
                PmsReportWorkHour hourModel = new PmsReportWorkHour();
                BeanUtils.copyProperties(hourDTO, hourModel);
                hourModel.setManHour(BigDecimal.valueOf(LocalDateTimeUtil.between(hourDTO.getStartTime(), hourDTO.getEndTime(), ChronoUnit.MINUTES)));
                hourModel.setReportWorkId(pmsReportWorkId);
                hourModelList.add(hourModel);
            }
            saveBatch(hourModelList);
        }
    }

    @Override
    public List<ProcessReportWorkHourDTO> getReportWorkHour(Long reportWorkId) {
        List<ProcessReportWorkHourDTO> resList = Lists.newArrayList();
        List<PmsReportWorkHour> dbList = list(new LambdaQueryWrapper<PmsReportWorkHour>().eq(PmsReportWorkHour::getReportWorkId, reportWorkId));
        if(CollUtil.isNotEmpty(dbList)){
            dbList.forEach(ele ->{
                ProcessReportWorkHourDTO resVo = new ProcessReportWorkHourDTO();
                BeanUtils.copyProperties(ele,resVo);
                resList.add(resVo);
            });
        }
        return resList;
    }
}





