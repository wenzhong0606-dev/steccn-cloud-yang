package com.steccn.platform.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.domain.HikResponse;
import com.steccn.platform.dto.HikDbDTO;
import com.steccn.platform.service.HikResponseService;
import com.steccn.platform.mapper.HikResponseMapper;
import com.steccn.wms.dto.WmsWeighbridgeInfoDTO;
import com.steccn.wms.feign.RemoteWmsStorageReceiptFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
* @author Admin
* @description 针对表【hik_response】的数据库操作Service实现
* @createDate 2024-05-16 09:39:14
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class HikResponseServiceImpl extends ServiceImpl<HikResponseMapper, HikResponse>
    implements HikResponseService{
    private final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(3, 5, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000), Executors.defaultThreadFactory());
    private final RemoteWmsStorageReceiptFeignApi remoteWmsStorageReceiptFeignApi;

    @Override
    public Boolean saveResponse(HikDbDTO dto) {
        HikResponse response = new HikResponse();
        //称重确认时间的id
        response.setType("79174070273");
        // 解析为 OffsetDateTime
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dto.getSendTime());
        response.setHappenTime(offsetDateTime.toLocalDateTime());
        JSONObject jsonObject = new JSONObject(dto);
        response.setContentJson(jsonObject.toString());
        save(response);
        //保存到仓储页面
        if(dto.getEvents().get(0).getData().getModelData().getWeighReg().getWeigh_status().equals(200)){
            poolExecutor.execute(()->{
                WmsWeighbridgeInfoDTO weighbridgeDTO = new WmsWeighbridgeInfoDTO();
                FeignOptional.of(()->remoteWmsStorageReceiptFeignApi.weighbridgeSave(weighbridgeDTO), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
            });

        }
        return true;
    }
}




