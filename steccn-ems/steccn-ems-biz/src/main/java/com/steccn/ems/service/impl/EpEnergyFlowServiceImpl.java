package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.common.vo.EpTreeGraphVO;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.dto.EpEnergyFlowDTO;
import com.steccn.ems.dto.EpTableDTO;
import com.steccn.ems.service.EpConsumePartitionChartService;
import com.steccn.ems.service.EpEnergyFlowService;
import com.steccn.ems.service.EpPartitionService;
import com.steccn.ems.vo.EpConsumeDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 能源流向impl
 *
 * @author AndrewLuo
 * @date 2022/12/12
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EpEnergyFlowServiceImpl implements EpEnergyFlowService {
    private final EpConsumePartitionChartService epConsumePartitionChartService;

    private final EpPartitionService epPartitionService;

    /**
     * 获取能源流向详情数据
     * @param dto
     * @return
     */
    @Override
    public EpConsumeDetailVO getDetails(EpEnergyFlowDTO dto) {
        EpConsumeDetailVO result = new EpConsumeDetailVO();
        List<EpPartition> epPartitionList = epPartitionService.list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getType, dto.getEnergyTypeId()));
        if(CollUtil.isEmpty(epPartitionList)){
            log.debug("节点id为空");
            return result;
        }
        List<Long> partitionIdList = Lists.newArrayList();
        epPartitionList.forEach(tmp -> {
            partitionIdList.add(tmp.getId());
        });
        //拿到该能源类型下所有的节点
        EpTableDTO tableDTO = new EpTableDTO();
        tableDTO.setStart(dto.getDateStart());
        tableDTO.setEnd(dto.getDateEnd());
        tableDTO.setEnergyType(dto.getEnergyTypeId());
        tableDTO.setPartitionIdList(partitionIdList);
        //拿到厚度
        List<BaseSelectVO<BigDecimal>> thick = epConsumePartitionChartService.thick(tableDTO);
        if(CollUtil.isEmpty(thick)){
            log.debug("厚度为空");
            return result;
        }
        List<BigDecimal> thickList = Lists.newArrayList();
        thick.forEach(tmp -> {
            thickList.add(tmp.getId());
        });
        tableDTO.setThick(thickList);
        EpConsumeDetailVO vo = epConsumePartitionChartService.consumePartitionContrastDetail(tableDTO);
        result = vo;
        return result;
    }

    /**
     * 生成树状图表数据
     * @param dto
     * @return
     */
    @Override
    public EpTreeGraphVO generateCharts(EpEnergyFlowDTO dto) {
        return null;
    }
}
