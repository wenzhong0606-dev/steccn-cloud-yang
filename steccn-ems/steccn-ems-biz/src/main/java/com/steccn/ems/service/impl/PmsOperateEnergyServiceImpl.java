package com.steccn.ems.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.MapUtil;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.common.util.UnitLengthUtil;
import com.steccn.common.vo.BaseValueNameVO;
import com.steccn.common.vo.BaseValueVO;
import com.steccn.common.vo.CommonBarLineVO;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.ems.domain.EpEnergyType;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.service.EpEnergyTypeService;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.service.EpPartitionService;
import com.steccn.ems.service.PmsOperateEnergyService;
import com.steccn.ems.vo.EpMeterReadAllDataVO;
import com.steccn.ems.vo.PmsOperateEnergyVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.XAxisUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @desc 能源驾驶舱
 * @date 2023/12/1
 */
@RequiredArgsConstructor
@Service
public class PmsOperateEnergyServiceImpl implements PmsOperateEnergyService {

    private final EpMeterReadService epMeterReadService;
    private final EpPartitionService epPartitionService;
    private final EpEnergyTypeService epEnergyTypeService;
    private final RemoteDictFeignApi remoteDictFeignApi;

    @Override
    public PmsOperateEnergyVO gas(DpPmsOperateDTO dto) {
        return energy(dto, 3, UnitLengthUtil.UNIT_CUBIC_METER_LENGTH);
    }

    @Override
    public PmsOperateEnergyVO oil(DpPmsOperateDTO dto) {
        return energy(dto, 4, UnitLengthUtil.UNIT_TON_LENGTH);
    }

    @Override
    public PmsOperateEnergyVO water(DpPmsOperateDTO dto) {
        return energy(dto, 2, UnitLengthUtil.UNIT_TON_LENGTH);
    }

    @Override
    public PmsOperateEnergyVO electric(DpPmsOperateDTO dto) {
        return energy(dto, 1, UnitLengthUtil.UNIT_CUBIC_METER_LENGTH);
    }

    /**
     * 能源值
     *
     * @param dto        dto
     * @param energyType 能源类型
     * @param bit        单位
     * @return
     */
    private PmsOperateEnergyVO energy(DpPmsOperateDTO dto, Integer energyType, Integer bit) {
        EpEnergyType energy = epEnergyTypeService.getById(energyType);
        Assert.notNull(energy, "能源类型不存在");
        Map<Long, String> measureUnitMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(Sets.newHashSet(energy.getMeasureUnit())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
        Assert.notEmpty(dateList, "日期区间有误");
        List<LocalDate> conditionDt = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), dto.getStart(), dto.getEnd());
        //根据能源类型 + 组织机构id获取
        EpPartition one = epPartitionService.getOne(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getOfficeId, dto.getOfficeId()).eq(EpPartition::getType, energy.getId()));
        Assert.notNull(one, "能源树，不存在此节点");
        //获取全量数据表
        List<EpMeterReadAllDataVO> classDateAllData = epMeterReadService.classDateAllData(Sets.newHashSet(conditionDt), energyType, Arrays.asList(one.getId()));
        SteccnTableAssertUtil.notEmpty(classDateAllData, "全量数据表为空");
        //构建能源树
        Map<String, BigDecimal> valueMap = Maps.newHashMap();
        classDateAllData.forEach(ele -> {
            String convert = XAxisUtil.convert(granularity, ele.getReadDate());
            MapUtil.add(valueMap, convert, ele.getConsume(), (x, y) -> BigDecimalUtil.add(x, y, bit));
        });
        List<String> xAisList = Lists.newArrayList();
        List<BigDecimal> valueList = Lists.newArrayList();
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate ele = dateList.get(i - 1);
            String convert = XAxisUtil.convert(granularity, ele);
            xAisList.add(convert);
            valueList.add(BigDecimalUtil.setScale(valueMap.get(convert), bit));
        }
        PmsOperateEnergyVO res = new PmsOperateEnergyVO();
        res.setTotalQuantity(BigDecimalUtil.setScale(BigDecimalUtil.sum(valueMap.values()), bit));
        res.setUnitName(measureUnitMap.getOrDefault(energy.getMeasureUnit(), Strings.EMPTY));
        CommonBarLineVO commonBarLineVO = new CommonBarLineVO();
        res.setCommonBarLineVO(commonBarLineVO);
        commonBarLineVO.setXAxis(new BaseValueVO(xAisList));
        commonBarLineVO.setLegend(new BaseValueVO(Arrays.asList(energy.getName() + "(" + res.getUnitName() + ")")));
        commonBarLineVO.setSeries(Arrays.asList(new BaseValueNameVO<>(valueList, energy.getName() + "(" + res.getUnitName() + ")")));
        return res;
    }
}
