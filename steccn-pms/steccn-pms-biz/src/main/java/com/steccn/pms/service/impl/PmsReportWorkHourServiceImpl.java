package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.pms.domain.PmsReportWorkHour;
import com.steccn.pms.dto.ProcessReportWorkHourDTO;
import com.steccn.pms.service.PmsReportWorkHourService;
import com.steccn.pms.mapper.PmsReportWorkHourMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateHour(Long reportWorkId, List<ProcessReportWorkHourDTO> hourDTOList) {
        //部分工时需要删除
        //部分工时需要编辑
        List<PmsReportWorkHour> editList = Lists.newArrayList();
        //部分工时需要新增
        List<PmsReportWorkHour> insertList = Lists.newArrayList();
        //最新需要保留的id集合
        Set<Long> newIdSet = Sets.newHashSet();
        if (CollUtil.isNotEmpty(hourDTOList)) {
            hourDTOList.forEach(ele -> {
                PmsReportWorkHour model = new PmsReportWorkHour();
                BeanUtils.copyProperties(ele, model);
                if (Objects.nonNull(ele.getId())) {
                    editList.add(model);
                } else {
                    model.setId(IdUtil.getSnowflake().nextId());
                    insertList.add(model);
                }
                model.setReportWorkId(reportWorkId);
                newIdSet.add(model.getId());
            });
        }

        //删除元素
        remove(new LambdaQueryWrapper<PmsReportWorkHour>().eq(PmsReportWorkHour::getReportWorkId, reportWorkId)
                .notIn(CollUtil.isNotEmpty(newIdSet), PmsReportWorkHour::getId, newIdSet));

        //新增元素
        if (CollUtil.isNotEmpty(insertList)) {
            saveBatch(insertList);
        }
        //编辑元素
        if (CollUtil.isNotEmpty(editList)) {
            updateBatchById(editList);
        }
    }
}





