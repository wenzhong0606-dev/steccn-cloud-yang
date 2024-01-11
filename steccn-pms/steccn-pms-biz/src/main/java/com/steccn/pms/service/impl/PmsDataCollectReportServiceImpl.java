package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.dto.SteccnTableColumn;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.pms.dto.DataCollectDTO;
import com.steccn.pms.service.PmsDataCollectReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2024/01/10
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PmsDataCollectReportServiceImpl implements PmsDataCollectReportService {
    @Override
    public SteccnTableDataDTO<Map<String, String>> turbineReport(LocalDate date) {
        List<Map<String, String>> data = postDataCollect(date, Lists.newArrayList("YR_DPU2_AI02_2_METER3"
                , "YR_DPU2_TC03_3_METER6"
                , "YR_DPU2_RT05_2_METER6"
                , "YR_DPU2_RT05_2_METER7"
                , "YR_DPU2_AI02_2_METER6"
                , "YR_DPU2_TC05_4_METER4"
                , "YR_DPU2_AI03_2_METER1"
                , "YR_DPU2_AI03_1_METER7"
                , "YR_DPU2_AI04_1_METER2"
                , "YR_DPU2_RT05_2_METER1"
                , "YR_DPU2_RT05_2_METER2"
                , "YR_DPU2_RT01_3_METER5"
                , "YR_DPU2_RT02_3_METER5"
                , "YR_DPU2_SH0027_AI_I"
                , "YR_DPU2_SH0029_AI_I"
                , "YR_DPU2_AI04_2_METER6"
                , "YR_DPU2_AI04_2_METER4"
                , "YR_SH0053_AI_I"
                , "YR_SH0055_AI_I"
                , "YR_SH0057_AI_I"
                , "YR_DPU2_AI04_2_METER7"));
        SteccnTableAssertUtil.notEmpty(data, "暂无数据");
        SteccnTableDataDTO<Map<String, String>> tableData = new SteccnTableDataDTO<>();
        //2.1、组表头
        List<SteccnTableColumn> columns = Lists.newArrayList();
        columns.add(new SteccnTableColumn("YR_DPU2_AI02_2_METER3","主蒸汽压力(Mpa)",null));
        columns.add(new SteccnTableColumn("YR_DPU2_TC03_3_METER6","主蒸汽温度(℃)",null));
        columns.add(new SteccnTableColumn("YR_DPU2_RT05_2_METER6","汽轮机前轴承温度",null));
        columns.add(new SteccnTableColumn("YR_DPU2_RT05_2_METER7","汽轮机后轴承温度",null));
        columns.add(new SteccnTableColumn("YR_DPU2_AI02_2_METER6","真空(KPa)",null));
        columns.add(new SteccnTableColumn("YR_DPU2_TC05_4_METER4","气缸排气温度",null));
        columns.add(new SteccnTableColumn("YR_DPU2_AI03_2_METER1","润滑油压力",null));
        columns.add(new SteccnTableColumn("YR_DPU2_AI03_1_METER7","油箱油位",null));
        columns.add(new SteccnTableColumn("YR_DPU2_AI04_1_METER2","轴向位移",null));
        columns.add(new SteccnTableColumn("YR_DPU2_RT05_2_METER1","冷油器进水温度",null));
        columns.add(new SteccnTableColumn("YR_DPU2_RT05_2_METER2","冷油器出水温度",null));
        columns.add(new SteccnTableColumn("YR_DPU2_RT01_3_METER5","凝结器进水温度",null));
        columns.add(new SteccnTableColumn("YR_DPU2_RT02_3_METER5","凝结器出水温度",null));
        columns.add(new SteccnTableColumn("YR_DPU2_SH0027_AI_I","1#凝结泵电流",null));
        columns.add(new SteccnTableColumn("YR_DPU2_SH0029_AI_I","2#凝结泵电流",null));
        columns.add(new SteccnTableColumn("YR_DPU2_AI04_2_METER6","凝结器压力(pa)",null));
        columns.add(new SteccnTableColumn("YR_DPU2_AI04_2_METER4","射水泵出口压力(Mpa)",null));
        columns.add(new SteccnTableColumn("YR_SH0053_AI_I","1#循环水泵电流",null));
        columns.add(new SteccnTableColumn("YR_SH0055_AI_I","2#循环水泵电流",null));
        columns.add(new SteccnTableColumn("YR_SH0057_AI_I","3#循环水泵电流",null));
        columns.add(new SteccnTableColumn("YR_DPU2_AI04_2_METER7","循环水泵出口压力",null));
        tableData.setColumns(columns);
        tableData.setTableList(data);
        return tableData;
    }

    protected List<Map<String,String>> postDataCollect(LocalDate date, List<String> points){
        List<Map<String, String>> result = Lists.newArrayList();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("date",date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
        jsonObject.put("dpNames",points);
        String res = HttpUtil.post("http://192.168.18.241:9001/getReportData", jsonObject.toJSONString());
        JSONObject resultObj = JSONObject.parseObject(res);
        if(Objects.isNull(resultObj)){
            return result;
        }
        if(!resultObj.getInteger("errCode").equals(0)){
            log.error(resultObj.getString("errMsg"));
            return result;
        }
        Map<String,List<DataCollectDTO>> mapResult = Maps.newHashMap();
        JSONArray jsonArray = resultObj.getJSONArray("datalist");
        for(int i = 0; i < jsonArray.size(); i++){
            JSONObject jsonObjectTmp = jsonArray.getJSONObject(i);
            if(jsonObjectTmp.getString("errorCode")==null){
                List<DataCollectDTO> dataTmp = jsonObjectTmp.getJSONArray("datas").toJavaList(DataCollectDTO.class);
                mapResult.put(jsonObjectTmp.getString("code"),dataTmp);
            }
        }
        //组数据
        //切割一天的时间
        List<LocalDateTime> hour = ShardTimeUtil.cutDateRangeNew("H", date.atTime(8, 0, 0), date.plusDays(1).atTime(7, 0, 0));
        for(LocalDateTime timeTmp:hour){
            Map<String,String> resultTmp = Maps.newHashMap();
            resultTmp.put("time",timeTmp.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            for(String point:points){
                List<DataCollectDTO> dataTmp = mapResult.get(point);
                if(CollUtil.isNotEmpty(dataTmp)){
                    BigDecimal valueTmp = dataTmp.stream().filter(v -> v.getTimestamp().equals(timeTmp)).findFirst().orElse(new DataCollectDTO()).getValue();
                    if(Objects.nonNull(valueTmp)){
                        resultTmp.put(point,valueTmp.setScale(2, RoundingMode.HALF_UP).toString());
                    }else{
                        resultTmp.put(point,null);
                    }
                }
            }
            result.add(resultTmp);
        }
        return result;
    }
}
