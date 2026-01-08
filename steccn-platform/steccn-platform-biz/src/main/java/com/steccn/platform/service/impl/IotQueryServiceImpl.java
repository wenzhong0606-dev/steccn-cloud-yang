package com.steccn.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.IotDataQueryDTO;
import com.steccn.platform.dto.IotDeviceDTO;
import com.steccn.platform.feign.RemoteIotFeignApi;
import com.steccn.platform.service.IotQueryService;
import com.steccn.platform.vo.IotDataQueryVO;
import com.steccn.platform.vo.IotIdNameVO;
import com.steccn.platform.vo.IotPointVO;
import com.steccn.pms.dto.PmsBillExcelDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数采平台查询相关
 *
 * @author AndrewLuo
 * @date 2025/11/11
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IotQueryServiceImpl implements IotQueryService {
    private static final String url = "http://60.171.128.8:82";
    private static final Map<String,String> header = Maps.newHashMap();
    static {
        header.put("X-ProjectID", "iotopo");
        header.put("X-API-KEY", "2k75l9b5dw6iyifh");
    }
    private static final Map<String,String> queryMethod = Maps.newHashMap();
    static {
        queryMethod.put("last", "最新值");
        queryMethod.put("first", "最旧值");
        queryMethod.put("min", "最小值");
        queryMethod.put("max", "最大值");
        queryMethod.put("mean", "平均值");
        queryMethod.put("spread", "变化值");
        queryMethod.put("stddev", "均方差");
    }

    @Override
    public List<IotIdNameVO> queryEqpGroup() {
        String body = HttpUtil.createGet(url + "/iot/api/v1/device_group/for_display").addHeaders(header).execute().body();
        List<IotIdNameVO> result = JSONObject.parseObject(body).getJSONArray("data").toJavaList(IotIdNameVO.class);
        //定制需求,只显示该装置
        result = result.stream().filter(v->!v.getId().equals("d2qgb1s91a2oav9cn7i0")).collect(Collectors.toList());
        return result;
    }

    @Override
    public Page<IotIdNameVO> queryDeviceInfo(IotDeviceDTO dto) {
        Map<String,Object> paramMap = Maps.newHashMap();
        paramMap.put("pageSize",dto.getSize());
        paramMap.put("pageNum",dto.getCurrent());
        if(StrUtil.isNotBlank(dto.getSearch())){
            paramMap.put("search",dto.getSearch());
        }
        if(StrUtil.isNotBlank(dto.getGroupId())){
            paramMap.put("groupID",dto.getGroupId());
        }
        String body = HttpUtil.createGet(url + "/iot/api/v1/device/query").addHeaders(header).form(paramMap).execute().body();
        List<IotIdNameVO> data = JSONObject.parseObject(body).getJSONObject("data").getJSONArray("items").toJavaList(IotIdNameVO.class);
        Page<IotIdNameVO> result = new Page<>();
        result.setRecords(data);
        result.setSize(dto.getSize());
        result.setCurrent(dto.getCurrent());
        result.setTotal(JSONObject.parseObject(body).getJSONObject("data").getLongValue("total"));
        return result;
    }

    @Override
    public Page<IotPointVO> queryPointInfo(IotDeviceDTO dto) {
        Map<String,Object> paramMap = Maps.newHashMap();
        paramMap.put("pageSize",dto.getSize());
        paramMap.put("pageNum",dto.getCurrent());
        if(StrUtil.isNotBlank(dto.getSearch())){
            paramMap.put("search",dto.getSearch());
        }
        if(StrUtil.isNotBlank(dto.getDeviceId())){
            paramMap.put("deviceID",dto.getDeviceId());
        }
        String body = HttpUtil.createGet(url + "/iot/api/v1/device_point/query").addHeaders(header).form(paramMap).execute().body();
        List<IotPointVO> data = JSONObject.parseObject(body).getJSONObject("data").getJSONArray("items").toJavaList(IotPointVO.class);
        Page<IotPointVO> result = new Page<>();
        result.setRecords(data);
        result.setSize(dto.getSize());
        result.setCurrent(dto.getCurrent());
        result.setTotal(JSONObject.parseObject(body).getJSONObject("data").getLongValue("total"));
        return result;
    }

    @Override
    public List<IotDataQueryVO> queryPointData(IotDataQueryDTO dto) {
        dto.setStart(toUTC(dto.getStart(),"Asia/Shanghai"));
        dto.setEnd(toUTC(dto.getEnd(),"Asia/Shanghai"));
        String body = HttpRequest.post(url + "/iot/api/v1/data/query")
                .addHeaders(header)
                .body(JSONUtil.toJsonStr(dto))
                .timeout(50000)
                .execute()
                .body();
        // Jackson 反序列化
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = null;
        try {
            root = mapper.readTree(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // 获取 results 数组
        JsonNode resultsNode = root
                .get("data")
                .get("results");

        List<IotDataQueryVO> list = Lists.newArrayList();

        // 转成目标类 List<IotDataQueryVO>
        try {
            list = mapper.readValue(
                    resultsNode.toString(),
                    mapper.getTypeFactory().constructCollectionType(List.class, IotDataQueryVO.class)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        for (IotDataQueryVO vo : list) {
            List<Map<String, Object>> valuesList = vo.getValues();
            if(CollUtil.isNotEmpty(valuesList)){
                for (Map<String, Object> map : valuesList) {
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        Object valueObj = entry.getValue();
                        if (valueObj instanceof Number) {
                            BigDecimal bd = new BigDecimal(((Number) valueObj).doubleValue());
                            bd = bd.setScale(2, RoundingMode.HALF_UP);
                            entry.setValue(bd.doubleValue()); // 保留两位小数
                        }
                    }
                }
            }
        }
        return list;
    }

    @Override
    public void excelExportData(HttpServletResponse response, List<IotDataQueryDTO> dto) throws IOException {
        List<List<IotDataQueryVO>> allData = Lists.newArrayList();
        //组成头部
        List<List<String>> headList = Lists.newArrayList();
        //头部的Key
        List<List<String>> headKeys = Lists.newArrayList();
        headList.add(com.google.common.collect.Lists.newArrayList("时间"));
        //请求数据
        for(IotDataQueryDTO tmp:dto){
            List<IotDataQueryVO> iotDataQueryVOS = queryPointData(tmp);
            allData.add(iotDataQueryVOS);
            for(String str:tmp.getPoints().get(0).getAggregations()){
                headList.add(com.google.common.collect.Lists.newArrayList(tmp.getPoints().get(0).getName()+"（"+queryMethod.get(str)+"）"));
            }
            headKeys.add(tmp.getPoints().get(0).getAggregations());
        }
        //组数据
        List<List<String>> rows = Lists.newArrayList();
        for(int i=0;i<allData.get(0).get(0).getValues().size();i++){
            List<String> row = Lists.newArrayList();
            for(int j=0;j<allData.size();j++){
                List<Map<String, Object>> values = allData.get(j).get(0).getValues();
                List<String> headKey = headKeys.get(j);
                if(j==0){
                    String time = values.get(i).getOrDefault("time", "").toString();
                    row.add(utcToLocal(time,"Asia/Shanghai"));
                }
                for(String str:headKey){
                    row.add(values.get(i).getOrDefault(str, "").toString());
                }
            }
            rows.add(row);
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        //防止中文乱码
        String fileName = URLEncoder.encode("数据结果", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream()).head(headList).sheet("sheet1").doWrite(rows);
    }

    /**
     * 将本地时间字符串（例如中国东八区）转换成 UTC ISO8601 字符串
     *
     * @param localTimeStr 例如 "2025-11-18 05:40:22"
     * @param localZoneId  本地时区，例如 "Asia/Shanghai"
     * @return UTC ISO8601 字符串，例如 "2025-11-17T21:40:22.000Z"
     */
    public static String toUTC(String localTimeStr, String localZoneId) {
        LocalDateTime ldt = LocalDateTime.parse(localTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        ZonedDateTime zdt = ldt.atZone(ZoneId.of(localZoneId));
        ZonedDateTime utc = zdt.withZoneSameInstant(ZoneId.of("UTC"));
        return utc.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"));
    }

    /**
     * 将 UTC ISO8601 时间转换为本地时间（例如：Asia/Shanghai）
     *
     * @param utcStr 例如 "2025-11-18T05:40:22.800Z"
     * @param zoneId 本地时区 例如 "Asia/Shanghai"
     * @return 转换后的时间 例如 "2025-11-18 13:40:22"
     */
    public static String utcToLocal(String utcStr, String zoneId) {
        ZonedDateTime utcZdt = ZonedDateTime.parse(utcStr, DateTimeFormatter.ISO_DATE_TIME);
        ZonedDateTime localZdt = utcZdt.withZoneSameInstant(ZoneId.of(zoneId));
        return localZdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
