package com.steccn.dp.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.dp.domain.RawNum;
import com.steccn.dp.service.RawNumService;
import com.steccn.dp.mapper.RawNumMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
* @author Admin
* @description 针对表【raw_num(原料每天的值)】的数据库操作Service实现
* @createDate 2025-04-02 14:11:41
*/
@Service
public class RawNumServiceImpl extends ServiceImpl<RawNumMapper, RawNum>
    implements RawNumService{

    @Override
    public Boolean test() {
        List<LocalDateTime> days = ShardTimeUtil.cutDateRange("D", LocalDate.of(2023,1,1).atTime(8,0,0), LocalDate.of(2025,4,1).atTime(8,0,0));
        //白云石、纯碱、石灰石的入料量
        Map<LocalDate, BigDecimal> map1 = Maps.newHashMap();
        Map<LocalDate, BigDecimal> map2 = Maps.newHashMap();
        Map<LocalDate, BigDecimal> map3 = Maps.newHashMap();
        List<Map<LocalDate, BigDecimal>> results = Lists.newArrayList();
        results.add(map1);
        results.add(map2);
        results.add(map3);
        for(int i = 0; i < days.size(); i++){
            Map<String, BigDecimal> mapResult = postWeightCount(new TupleDTO<>(days.get(i), days.get(i).plusDays(1).withHour(7).withMinute(59).withSecond(59)));
            RawNum model = new RawNum();
            model.setDay(days.get(i).toLocalDate());
            model.setCode("data3");
            model.setValue(BigDecimalUtil.divide(mapResult.get("data3"),BigDecimalUtil.THOUSAND));
            save(model);
            RawNum model2 = new RawNum();
            model2.setDay(days.get(i).toLocalDate());
            model2.setCode("data4");
            model2.setValue(BigDecimalUtil.divide(mapResult.get("data4"),BigDecimalUtil.THOUSAND));
            save(model2);
            RawNum model3 = new RawNum();
            model3.setDay(days.get(i).toLocalDate());
            model3.setCode("data5");
            model3.setValue(BigDecimalUtil.divide(mapResult.get("data5"),BigDecimalUtil.THOUSAND));
            save(model3);
        }
        return null;
    }

    @Override
    public Boolean insertToday() {
        LocalDate now = LocalDate.now();
        remove(new LambdaQueryWrapper<RawNum>().eq(RawNum::getDay,now));
        List<LocalDateTime> days = ShardTimeUtil.cutDateRange("D", now.minusDays(1).atTime(8,0,0), now.atTime(8,0,0));
        //白云石、纯碱、石灰石的入料量
        Map<LocalDate, BigDecimal> map1 = Maps.newHashMap();
        Map<LocalDate, BigDecimal> map2 = Maps.newHashMap();
        Map<LocalDate, BigDecimal> map3 = Maps.newHashMap();
        List<Map<LocalDate, BigDecimal>> results = Lists.newArrayList();
        results.add(map1);
        results.add(map2);
        results.add(map3);
        for(int i = 0; i < days.size(); i++){
            Map<String, BigDecimal> mapResult = postWeightCount(new TupleDTO<>(days.get(i), days.get(i).plusDays(1).withHour(7).withMinute(59).withSecond(59)));
            RawNum model = new RawNum();
            model.setDay(days.get(i).toLocalDate());
            model.setCode("data3");
            model.setValue(BigDecimalUtil.divide(mapResult.get("data3"),BigDecimalUtil.THOUSAND));
            save(model);
            RawNum model2 = new RawNum();
            model2.setDay(days.get(i).toLocalDate());
            model2.setCode("data4");
            model2.setValue(BigDecimalUtil.divide(mapResult.get("data4"),BigDecimalUtil.THOUSAND));
            save(model2);
            RawNum model3 = new RawNum();
            model3.setDay(days.get(i).toLocalDate());
            model3.setCode("data5");
            model3.setValue(BigDecimalUtil.divide(mapResult.get("data5"),BigDecimalUtil.THOUSAND));
            save(model3);
        }
        return true;
    }

    public Map<String, BigDecimal> postWeightCount(TupleDTO<LocalDateTime,LocalDateTime> dto){
        Map<String,BigDecimal> result = Maps.newHashMap();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("start",dto.getField1().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")));
        jsonObject.put("stop",dto.getField2().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")));
        String res = HttpUtil.post("http://192.168.18.241:9001/getMaterialDetail", jsonObject.toJSONString());
        JSONObject resultObj = JSONObject.parseObject(res);
        if(Objects.isNull(resultObj)){
            return result;
        }
        if(!resultObj.getInteger("errCode").equals(0)){
            log.error(resultObj.getString("errMsg"));
            return result;
        }
        JSONArray jsonArray = resultObj.getJSONArray("data");
        for(int i = 0; i < jsonArray.size(); i++){
            int j = i+1;
            String tmp = "data"+j;
            result.put(tmp,jsonArray.getJSONObject(i).getBigDecimal(tmp));
        }
        return result;
    }
}




