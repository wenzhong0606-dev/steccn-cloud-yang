package com.steccn.sales.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.StackGraphDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.common.util.StackGraphUtil;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.dp.vo.DpValueDateVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.MysqlDateKeyUtil;
import com.steccn.qms.util.XAxisUtil;
import com.steccn.sales.domain.KsContract;
import com.steccn.sales.service.KsContractService;
import com.steccn.sales.mapper.KsContractMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
* @author Admin
* @description 针对表【ks_contract(合同)】的数据库操作Service实现
* @createDate 2024-08-26 14:17:37
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class KsContractServiceImpl extends ServiceImpl<KsContractMapper, KsContract>
    implements KsContractService{
    private final KsContractMapper ksContractMapper;

    @Override
    public QtLineChartBarDTO contractSum(DpPmsOperateDTO dto) {
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularity, "粒度不存在");
        //获取数据
        List<TupleDTO<String,BigDecimal>> contractCount;
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            contractCount = ksContractMapper.contractCountDay(dto.getStart(),dto.getEnd());
        } else if (QtManagerConstant.QtGranularityEnum.WEEK.equals(granularity)) {
            contractCount = ksContractMapper.contractCountWeek(dto.getStart(),dto.getEnd());
        } else if (QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity)) {
            contractCount = ksContractMapper.contractCountMonth(dto.getStart(),dto.getEnd());
        } else {
            contractCount = ksContractMapper.contractCountYear(dto.getStart(),dto.getEnd());
        }

        if(CollUtil.isEmpty(contractCount)){
            SteccnTableAssertUtil.notEmpty(Lists.newArrayList(), "暂无数据");
        }
        BigDecimal totalQty = contractCount.stream().map(TupleDTO::getField2).reduce(BigDecimal.ZERO,BigDecimal::add);
        List<StackGraphDTO<String, BigDecimal>> dataList = Lists.newArrayList();
        List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
//        if(dto.getStart().equals(dto.getEnd())&&CollUtil.isEmpty(dateList)){
//            dateList.add(dto.getStart());
//        }
        //
        Map<Integer,BigDecimal> weightCountMap = Maps.newHashMap();
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate start = dateList.get(i - 1);
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            stackGraphDTO.setXAxis(XAxisUtil.convert(granularity, start));
            String key = MysqlDateKeyUtil.convert(granularity, start);
            List<TupleDTO<String, BigDecimal>> datList = Lists.newArrayList();
            //下单量
            BigDecimal weightCountTmp = BigDecimal.ZERO;
            TupleDTO<String,BigDecimal> valueDateVO = contractCount.stream().filter(v -> v.getField1().equals(key)).findFirst().orElse(null);
            if(Objects.nonNull(valueDateVO)){
                weightCountTmp = valueDateVO.getField2();
                BigDecimalUtil.accumulate(weightCountMap,1,weightCountTmp);
            }
            datList.add(new TupleDTO<>("下单量", weightCountTmp));
            datList.add(new TupleDTO<>("百分比", BigDecimalUtil.percent(weightCountTmp,totalQty,2)));
            stackGraphDTO.setData(datList);
            dataList.add(stackGraphDTO);
        }
        QtLineChartBarDTO<String,BigDecimal> chat = StackGraphUtil.convertData(dataList, Boolean.TRUE);
        chat.setOther(totalQty);
        return chat;
    }
}




