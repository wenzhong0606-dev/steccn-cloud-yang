package com.steccn.ems.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.MapUtil;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.common.vo.BaseValueNameVO;
import com.steccn.common.vo.BaseValueVO;
import com.steccn.common.vo.CommonBarLineVO;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.service.EpPartitionService;
import com.steccn.ems.service.PmsOperateEnergyService;
import com.steccn.ems.vo.EpMeterReadAllDataVO;
import com.steccn.ems.vo.PmsOperateEnergyVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.XAxisUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
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

    @Override
    public PmsOperateEnergyVO gas(DpPmsOperateDTO dto) {
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
        Assert.notEmpty(dateList, "日期区间有误");
        //根据能源类型 + 组织机构id获取
        EpPartition one = epPartitionService.getOne(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getOfficeId, dto.getOfficeId()).eq(EpPartition::getType, 3));
        Assert.notNull(one, "能源树，不存在此节点");
        //获取全量数据表
        List<EpMeterReadAllDataVO> classDateAllData = epMeterReadService.classDateAllData(Sets.newHashSet(dateList), 3, Arrays.asList(one.getId()));
        SteccnTableAssertUtil.notEmpty(classDateAllData, "全量数据表为空");
        //构建能源树
        Map<String, BigDecimal> valueMap = Maps.newHashMap();
        classDateAllData.forEach(ele -> {
            String convert = XAxisUtil.convert(granularity, ele.getReadDate());
            MapUtil.add(valueMap, convert, ele.getConsume(), (x, y) -> BigDecimalUtil.add(x, y, BigDecimalUtil.SCALE_0));
        });
        List<String> xAisList = Lists.newArrayList();
        List<BigDecimal> valueList = Lists.newArrayList();
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate ele = dateList.get(i - 1);
            String convert = XAxisUtil.convert(granularity, ele);
            xAisList.add(convert);
            valueList.add(valueMap.get(convert));
        }
        PmsOperateEnergyVO res = new PmsOperateEnergyVO();
        res.setTotalQuantity(BigDecimalUtil.sum(valueMap.values()));
        res.setUnitName("m³");
        CommonBarLineVO commonBarLineVO = new CommonBarLineVO();
        res.setCommonBarLineVO(commonBarLineVO);
        commonBarLineVO.setXAxis(new BaseValueVO(xAisList));
        commonBarLineVO.setLegend(new BaseValueVO(Arrays.asList("天然气")));
        commonBarLineVO.setSeries(Arrays.asList(new BaseValueNameVO<>(valueList, "天然气")));
        return res;
    }
}
