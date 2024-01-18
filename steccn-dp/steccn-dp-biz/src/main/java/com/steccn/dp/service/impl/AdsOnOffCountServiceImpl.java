package com.steccn.dp.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.common.dto.TupleDTO;
import com.steccn.dp.domain.AdsOnOffCount;
import com.steccn.dp.service.AdsOnOffCountService;
import com.steccn.dp.mapper.AdsOnOffCountMapper;
import com.steccn.dp.vo.DpOnOffCountVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
* @author Admin
* @description 针对表【ads_on_off_count(对开关数据的计数统计)】的数据库操作Service实现
* @createDate 2024-01-17 15:04:19
*/
@Service
public class AdsOnOffCountServiceImpl extends ServiceImpl<AdsOnOffCountMapper, AdsOnOffCount>
    implements AdsOnOffCountService{

    @Override
    public List<DpOnOffCountVO> onOffList(TupleDTO<LocalDate, LocalDate> dto) {
        List<DpOnOffCountVO> result = Lists.newArrayList();
        List<AdsOnOffCount> data = list(new LambdaQueryWrapper<AdsOnOffCount>()
                .ge(AdsOnOffCount::getDt, dto.getField1().toString())
                .le(AdsOnOffCount::getDt, dto.getField2().toString())
                .eq(AdsOnOffCount::getValue, "true")
                .in(AdsOnOffCount::getTagId, Lists.newArrayList("DEEP01_Q.GDGXH.SJG01_1GD102-2"
                        , "DEEP01_Q.GDGXH.SJG01_1GD202-2"
                        , "DEEP02_Q.GDGXH.SJG02_1GD102-2"
                        , "DEEP02_Q.GDGXH.SJG02_1GD202-2"
                        , "DEEP03_Q.GDGXH.SJG03_1GD102-2"
                        , "DEEP03_Q.GDGXH.SJG03_1GD202-2"
                        , "DEEP04_Q.GDGXH.SJG04_1GD102-2"
                        , "DEEP04_Q.GDGXH.SJG04_1GD202-2"
                        , "DEEP05_Q.GDGXH.SJG05_1GD102-2"
                        , "DEEP05_Q.GDGXH.SJG05_1GD202-2"
                        , "DEEP06_Q.GDGXH.SJG06_1GD102-2"
                        , "DEEP06_Q.GDGXH.SJG06_1GD202-2"
                        , "DEEP07_Q.GDGXH.SJG07_1GD102-2"
                        , "DEEP07_Q.GDGXH.SJG07_1GD202-2"
                        , "DEEP08_Q.GDGXH.SJG08_1GD102-2"
                        , "DEEP08_Q.GDGXH.SJG08_1GD202-2")));
        if(CollUtil.isNotEmpty(data)){
            data.forEach(tmp->{
                DpOnOffCountVO vo = new DpOnOffCountVO();
                vo.setNum(tmp.getValueCount());
                vo.setProductLineNum(tmp.getProductLine());
                vo.setClassDate(LocalDate.parse(tmp.getDt(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                String[] split = tmp.getStartToEnd().split("~");
                String start = split[0];
                String end = split[1];
                if(split[0].length()!=5){
                    start = "0" + start;
                }
                if(split[1].length()!=5){
                    end = "0" + end;
                }
                vo.setStart(LocalTime.parse(start,DateTimeFormatter.ofPattern("HH:mm")));
                vo.setEnd(LocalTime.parse(end,DateTimeFormatter.ofPattern("HH:mm")));
                result.add(vo);
            });
        }
        return result;
    }
}




