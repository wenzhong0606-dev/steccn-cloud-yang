package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.platform.dto.SysDictDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.constant.PmReportConstant;
import com.steccn.pms.domain.PmsReportWorkConsume;
import com.steccn.pms.dto.PmsDayReportDTO;
import com.steccn.pms.dto.ProcessReportWorkConsumeDTO;
import com.steccn.pms.service.PmsReportWorkConsumeService;
import com.steccn.pms.mapper.PmsReportWorkConsumeMapper;
import com.steccn.pms.vo.EnergyGroupListVO;
import com.steccn.pms.vo.ProduceQuantityVO;
import com.steccn.pms.vo.SubstrateDayReportListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_report_work_consume(报工能耗表)】的数据库操作Service实现
 * @createDate 2022-11-02 15:32:08
 */
@Service
@RequiredArgsConstructor
public class PmsReportWorkConsumeServiceImpl extends ServiceImpl<PmsReportWorkConsumeMapper, PmsReportWorkConsume>
        implements PmsReportWorkConsumeService {

    private final RemoteDictFeignApi remoteDictFeignApi;

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
        List<EnergyGroupListVO> groupList = this.baseMapper.energyGroup(productLineIdList, dateRangeList);
        return Objects.isNull(groupList) ? Lists.newArrayList() : groupList;
    }

    @Override
    public List<SubstrateDayReportListVO> substrateEnergy(PmsDayReportDTO dto, List<SysDictVO> classList, SysOfficeVO productLine) {
        List<SubstrateDayReportListVO> resList = Lists.newArrayList();

        List<EnergyGroupListVO> groupList = this.baseMapper.substrateEnergy(productLine.getId(), dto.getDate());
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
            SubstrateDayReportListVO gasVo = rowData(productLine, Lists.newArrayList(morning, night, BigDecimalUtil.add(morning, night)), consumeEnum.getName());
            resList.add(gasVo);
        }
        return resList;
    }


    /**
     * 构建行数据
     *
     * @param productLine
     * @param dataList
     * @param specStr
     * @return
     */
    private SubstrateDayReportListVO rowData(SysOfficeVO productLine, List<BigDecimal> dataList, String specStr) {
        SubstrateDayReportListVO row = new SubstrateDayReportListVO();
        row.setWorkshopName(productLine.getParentName());
        row.setProductLineName(productLine.getOfficeName());
        row.setType(PmReportConstant.TYPE_1);
        row.setSpecList(Arrays.asList(specStr, specStr, specStr));
        String dataStr1 = String.valueOf(dataList.get(0));
        String dataStr2 = String.valueOf(dataList.get(1));
        String dataStr3 = String.valueOf(dataList.get(2));
        row.setMorning(new ProduceQuantityVO(Arrays.asList(dataStr1, dataStr1)));
        row.setNight(new ProduceQuantityVO(Arrays.asList(dataStr2, dataStr2)));
        row.setTotal(new ProduceQuantityVO(Arrays.asList(dataStr3, dataStr3)));
        return row;
    }

}




