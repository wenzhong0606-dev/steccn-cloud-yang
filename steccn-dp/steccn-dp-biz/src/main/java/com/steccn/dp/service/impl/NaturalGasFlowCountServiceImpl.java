package com.steccn.dp.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.common.dto.TupleDTO;
import com.steccn.dp.domain.NaturalGasFlowCount;
import com.steccn.dp.service.NaturalGasFlowCountService;
import com.steccn.dp.mapper.NaturalGasFlowCountMapper;
import com.steccn.dp.vo.DpNatureGasVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author Admin
* @description 针对表【natural_gas_flow_count(天然气每小时流量均值统计)】的数据库操作Service实现
* @createDate 2024-05-27 15:57:48
*/
@Service
public class NaturalGasFlowCountServiceImpl extends ServiceImpl<NaturalGasFlowCountMapper, NaturalGasFlowCount>
    implements NaturalGasFlowCountService{

    @Override
    public List<DpNatureGasVO> natureGas(TupleDTO<LocalDateTime, LocalDateTime> dto) {
        List<NaturalGasFlowCount> list = list(new LambdaQueryWrapper<NaturalGasFlowCount>().between(NaturalGasFlowCount::getDt, dto.getField1(), dto.getField2()));
        List<DpNatureGasVO> result = Lists.newArrayList();
        //切割字符串
        if(CollUtil.isNotEmpty(list)){
            list.forEach(tmp->{
                DpNatureGasVO vo = new DpNatureGasVO();
                String s = tmp.getId().split("\\.")[2].split("_")[0];
                vo.setPointId("RY_"+s+"_PV");
                vo.setTime(tmp.getDt());
                vo.setValue(tmp.getIdValue());
                result.add(vo);
            });
        }
        return result;
    }
}




