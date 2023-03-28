package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.platform.dto.SysDictDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.constant.PmReportConstant;
import com.steccn.pms.domain.PmsReportWorkConsume;
import com.steccn.pms.domain.PmsReportWorkHour;
import com.steccn.pms.dto.PmsDayReportDTO;
import com.steccn.pms.dto.ProcessReportWorkConsumeDTO;
import com.steccn.pms.service.PmsPullForceService;
import com.steccn.pms.service.PmsReportWorkConsumeService;
import com.steccn.pms.mapper.PmsReportWorkConsumeMapper;
import com.steccn.pms.service.ReportService;
import com.steccn.pms.vo.EnergyGroupListVO;
import com.steccn.pms.vo.ProduceQuantityVO;
import com.steccn.pms.vo.SubstrateDayReportListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_report_work_consume(报工能耗表)】的数据库操作Service实现
 * @createDate 2022-11-02 15:32:08
 */
@Service
public class PmsReportWorkConsumeServiceImpl extends ServiceImpl<PmsReportWorkConsumeMapper, PmsReportWorkConsume>
        implements PmsReportWorkConsumeService {

    @Autowired
    private ReportService reportService;

    @Autowired
    private PmsPullForceService pmsPullForceService;

    @Override
    public void saveConsume(Long id, List<ProcessReportWorkConsumeDTO> consumeDTOList) {
        if (CollUtil.isNotEmpty(consumeDTOList)) {
            List<PmsReportWorkConsume> insertList = Lists.newArrayList();
            for (ProcessReportWorkConsumeDTO consume : consumeDTOList) {
                PmsReportWorkConsume model = new PmsReportWorkConsume();
                BeanUtils.copyProperties(consume, model);
                model.setReportWorkId(id);
                insertList.add(model);
            }
            saveBatch(insertList);
        }
    }

    @Override
    public List<EnergyGroupListVO> energyGroup(List<Integer> productLineIdList, List<LocalDateTime> dateRangeList) {
        //返回根据产线，班次，能耗类型分组的信息，包括产线，班组，能耗类型，能耗值，拉引量
        List<EnergyGroupListVO> allDataList = this.baseMapper.energyGroup(productLineIdList, dateRangeList);
        if (CollUtil.isNotEmpty(allDataList)) {
            //获取产线+班次集合
            Set<String> pfSet = Sets.newHashSet();
            allDataList.forEach(ele -> {
                LocalDate classDate = LocalDateUtil.getClassDate(ele.getStartTime(), Boolean.FALSE);
                pfSet.add(classDate + "," + ele.getProductLineId() + "," + ele.getClassesId());
                ele.setClassDate(classDate);
            });
            //根据班次日期+产线+班次查询拉引量
            Map<String,BigDecimal> pullForceMap = pmsPullForceService.getPullForce(pfSet);
            allDataList.forEach(ele ->{
                ele.setPullForce(pullForceMap.getOrDefault(ele.getClassDate() + "," + ele.getProductLineId() + "," + ele.getClassesId(), BigDecimal.ZERO));
            });
        }
        return Objects.isNull(allDataList) ? Lists.newArrayList() : allDataList;
    }

    @Override
    public List<SubstrateDayReportListVO> substrateEnergy(PmsDayReportDTO dto, List<SysDictVO> classList, SysOfficeVO productLine, Map<Integer, BigDecimal> workshopCountMap) {
        List<SubstrateDayReportListVO> resList = Lists.newArrayList();

        List<EnergyGroupListVO> groupList = this.baseMapper.substrateEnergy(dto.getClassGroupIdList(),productLine.getId(), dto.getDate());
        Map<Integer, List<EnergyGroupListVO>> typeMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(groupList)) {
            typeMap = groupList.stream().collect(Collectors.groupingBy(EnergyGroupListVO::getType));
        }
        //0耗电量 1耗水量 2耗油量 3耗气量
        for (PmReportConstant.ReportWorkConsumeEnum consumeEnum : PmReportConstant.ReportWorkConsumeEnum.values()) {
            List<EnergyGroupListVO> classesDataList = typeMap.getOrDefault(consumeEnum.getCode(), Lists.newArrayList());
            Map<Long, BigDecimal> classesDataMap = Maps.newHashMap();
            classesDataList.forEach(ele -> classesDataMap.put(ele.getClassesId(), ele.getQuantity()));
            //遍历班次【早班晚班】
            BigDecimal morning = classesDataMap.getOrDefault(classList.get(0).getId(), BigDecimal.ZERO);
            BigDecimal night = classesDataMap.getOrDefault(classList.get(1).getId(), BigDecimal.ZERO);
            SubstrateDayReportListVO dayVo = reportService.rowData(productLine, Lists.newArrayList(morning, night, BigDecimalUtil.add(morning, night)), consumeEnum.getName());
            BigDecimalUtil.accumulate(workshopCountMap, consumeEnum.getCode() + 2, BigDecimalUtil.add(morning, night));
            resList.add(dayVo);
        }
        return resList;
    }

    @Override
    public List<ProcessReportWorkConsumeDTO> getConsumeList(Long id) {
        List<ProcessReportWorkConsumeDTO> resList = null;
        List<PmsReportWorkConsume> dbList = list(new LambdaQueryWrapper<PmsReportWorkConsume>().eq(PmsReportWorkConsume::getReportWorkId, id));
        if (CollUtil.isNotEmpty(dbList)) {
            resList = BeanUtil.copyToList(dbList, ProcessReportWorkConsumeDTO.class);
        }
        return Objects.nonNull(resList) ? resList : Lists.newArrayList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConsume(Long reportWorkId, List<ProcessReportWorkConsumeDTO> consumeDTOList) {
        //0耗电量 1耗水量 2耗油量 3耗气量
        Assert.isTrue(consumeDTOList.size() <= 4, "最多只能插入4种类型能耗");
        //最多4个元素，最少可以0个元素，先正常操作 最后查询判断
        //部分工时需要删除
        //部分工时需要编辑
        List<PmsReportWorkConsume> editList = Lists.newArrayList();
        //部分工时需要新增
        List<PmsReportWorkConsume> insertList = Lists.newArrayList();
        //最新需要保留的id集合
        Set<Long> newIdSet = Sets.newHashSet();
        if (CollUtil.isNotEmpty(consumeDTOList)) {
            consumeDTOList.forEach(ele -> {
                PmsReportWorkConsume model = new PmsReportWorkConsume();
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
        remove(new LambdaQueryWrapper<PmsReportWorkConsume>().eq(PmsReportWorkConsume::getReportWorkId, reportWorkId)
                .notIn(CollUtil.isNotEmpty(newIdSet), PmsReportWorkConsume::getId, newIdSet));

        //新增元素
        if (CollUtil.isNotEmpty(insertList)) {
            saveBatch(insertList);
        }
        //编辑元素
        if (CollUtil.isNotEmpty(editList)) {
            updateBatchById(editList);
        }
        //最后再校验能耗类型不能重复
        Assert.isTrue(!consumeTypeRepeat(reportWorkId), "能耗类型不能重复");
    }

    /**
     * 报工类型是否重复
     * @param reportWorkId 报工主键
     * @return 是否重复
     */
    private boolean consumeTypeRepeat(Long reportWorkId) {
        List<PmsReportWorkConsume> dbList = list(new QueryWrapper<PmsReportWorkConsume>()
                .select("type, count(1) as id")
                .lambda().
                eq(PmsReportWorkConsume::getReportWorkId, reportWorkId)
                .groupBy(PmsReportWorkConsume::getType));
        if (CollUtil.isNotEmpty(dbList)) {
            for (PmsReportWorkConsume ele : dbList) {
                if (ele.getId() > 1) {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }
}




