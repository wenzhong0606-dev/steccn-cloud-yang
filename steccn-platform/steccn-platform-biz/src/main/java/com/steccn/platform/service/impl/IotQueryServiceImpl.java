package com.steccn.platform.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.IotDeviceDTO;
import com.steccn.platform.feign.RemoteIotFeignApi;
import com.steccn.platform.service.IotQueryService;
import com.steccn.platform.vo.IotIdNameVO;
import com.steccn.platform.vo.IotPointVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    private static final Map header = Maps.newHashMap();
    static {
        header.put("X-ProjectID", "iotopo");
        header.put("X-API-KEY", "2k75l9b5dw6iyifh");
    }

    @Override
    public List<IotIdNameVO> queryEqpGroup() {
        String body = HttpUtil.createGet(url + "/iot/api/v1/device_group/for_display").addHeaders(header).execute().body();
        List<IotIdNameVO> result = JSONObject.parseObject(body).getJSONArray("data").toJavaList(IotIdNameVO.class);
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
}
