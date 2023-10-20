package com.steccn.mes.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.StackGraphDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.StackGraphUtil;
import com.steccn.mes.domain.MesDefect;
import com.steccn.mes.domain.MesEquipment;
import com.steccn.mes.dto.MesDefectChartDTO;
import com.steccn.mes.dto.MesDefectCountDTO;
import com.steccn.mes.dto.MesInsTaskCountDTO;
import com.steccn.mes.dto.MesInsTaskParamDTO;
import com.steccn.mes.mapper.MesEquipmentMapper;
import com.steccn.mes.service.MesDefectService;
import com.steccn.mes.mapper.MesDefectMapper;
import com.steccn.oa.constant.OaConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【mes_defect(缺陷管理表)】的数据库操作Service实现
* @createDate 2023-10-20 16:01:23
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class MesDefectServiceImpl extends ServiceImpl<MesDefectMapper, MesDefect>
    implements MesDefectService{
    private final MesDefectMapper mesDefectMapper;
    private final MesEquipmentMapper mesEquipmentMapper;

    @Override
    public QtLineChartBarDTO defectChart(MesDefectChartDTO dto) {
        //获取数据
        MesInsTaskParamDTO param = new MesInsTaskParamDTO();
        param.setStartTime(dto.getStartTime());
        param.setEndTime(dto.getEndTime());
        param.setStatusNe(OaConstant.NUM_1);
        //所有缺陷数据
        List<MesDefectCountDTO> allDefect = mesDefectMapper.findDefectCount(param);
        Assert.notEmpty(allDefect,"数据为空");
        Set<String> eqpIdSet = allDefect.stream().map(MesDefectCountDTO::getEqpId).collect(Collectors.toSet());
        //获取所有的工厂建模
        List<MesEquipment> plants = mesEquipmentMapper.selectList(new LambdaQueryWrapper<MesEquipment>()
                .select(MesEquipment::getId, MesEquipment::getEquipmentName)
                .eq(MesEquipment::getIsEqp, OaConstant.NUM_1.toString()));
        //获取设备list
        List<MesEquipment> equipments = mesEquipmentMapper.selectList(new LambdaQueryWrapper<MesEquipment>()
                .select(MesEquipment::getId, MesEquipment::getParentCodes)
                .in(MesEquipment::getId,eqpIdSet));
        //设备对应的工厂建模
        Map<String,String> eqpPlantMap = Maps.newHashMap();
        equipments.forEach(tmp->{
            String[] codeArr = tmp.getParentCodes().split(",");
            for (int i = codeArr.length - 1; i >= 0; i--){
                String eqpCode = codeArr[i];
                MesEquipment plant = plants.stream().filter(v -> v.getId().toString().equals(eqpCode)).findFirst().orElse(null);
                if(Objects.nonNull(plant)){
                    eqpPlantMap.put(eqpCode,plant.getEquipmentName());
                    break;
                }
            }
        });
        //获取完成的数据
        param.setStatusNe(null);
        param.setStatus(OaConstant.NUM_5);
        List<MesDefectCountDTO> finishedDefect = mesDefectMapper.findDefectCount(param);
        //全部count
        Map<String, BigDecimal> allCountMap = Maps.newHashMap();
        //所有count
        Map<String, BigDecimal> finishedCountMap = Maps.newHashMap();
        allDefect.forEach(tmp->{
            String officeCode = eqpPlantMap.get(tmp.getEqpId());
            BigDecimalUtil.accumulate(allCountMap,officeCode,new BigDecimal(tmp.getNum()));
        });
        //完成数count
        finishedDefect.forEach(tmp->{
            String officeCode = eqpPlantMap.get(tmp.getEqpId());
            BigDecimalUtil.accumulate(finishedCountMap,officeCode,new BigDecimal(tmp.getNum()));
        });
        //开始组图表
        List<StackGraphDTO<String, BigDecimal>> dataList = Lists.newArrayList();
        for (Map.Entry<String, BigDecimal> entry : allCountMap.entrySet()) {
            String key = entry.getKey();
            //组数据
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            stackGraphDTO.setXAxis(entry.getKey());
            List<TupleDTO<String, BigDecimal>> datList = Lists.newArrayList();
            BigDecimal all = allCountMap.get(key);
            BigDecimal finished = finishedCountMap.get(key);
            datList.add(new TupleDTO<>("缺陷总数", all));
            datList.add(new TupleDTO<>("缺陷完成数", finished));
            datList.add(new TupleDTO<>("完成率", BigDecimalUtil.percent(finished,all,2)));
            stackGraphDTO.setData(datList);
            dataList.add(stackGraphDTO);
        }
        return StackGraphUtil.convertData(dataList, Boolean.TRUE);
    }
}




