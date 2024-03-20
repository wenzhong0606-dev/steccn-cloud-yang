package com.steccn.websocket.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.websocket.dto.OnlineUserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * @author yequan
 * @desc 第三方接口支持服务
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class WsThirdPartyService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final OaInfoService oaInfoService;

    public Page<OnlineUserDTO> online(Integer current, Integer size) {
        Set<String> keySet = redisTemplate.keys("ACTIVE_WEBSOCKET_*");
        List<OnlineUserDTO> resList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(keySet)) {
            Map<Long, Long> valueMap = Maps.newHashMap();
            keySet.forEach(ele -> {
                List<String> range = redisTemplate.opsForList().range(ele, 0, 1000);
                if (CollUtil.isNotEmpty(range)) {
                    range.forEach(aa -> {
                        try {
                            TupleDTO<Long, Long> tupleDTO = objectMapper.readValue(aa, new TypeReference<TupleDTO<Long, Long>>() {
                            });
                            if (!valueMap.containsKey(tupleDTO.getField1())) {
                                valueMap.put(tupleDTO.getField1(), tupleDTO.getField2());
                            } else {
                                //存在旧的值，但是旧的值时间戳大于当前时间戳，则覆盖
                                Long oldTs = valueMap.get(tupleDTO.getField1());
                                if (Objects.nonNull(oldTs) && Objects.nonNull(tupleDTO.getField2()) && oldTs > tupleDTO.getField2()) {
                                    valueMap.put(tupleDTO.getField1(), tupleDTO.getField2());
                                }
                            }
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    });
                }
            });
            log.info("START_OA");
            long ll = System.currentTimeMillis();
            Map<Long, String> userIdNameMap = FeignOptional.of(() -> oaInfoService.listByIdCh(Lists.newArrayList(valueMap.keySet())), FeignConstant.OA_SERVICE_NAME).get();
            log.info("END_OA ->{}", (System.currentTimeMillis() - ll));
            //遍历元素返回
            valueMap.forEach((k, v) -> {
                OnlineUserDTO onlineUserDTO = new OnlineUserDTO();
                onlineUserDTO.setUserId(k);
                Instant instant = Instant.ofEpochMilli(v);
                LocalDateTime linkTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                onlineUserDTO.setLinkTime(linkTime);
                String realName = userIdNameMap.get(k);
                if (StrUtil.isNotBlank(realName)) {
                    onlineUserDTO.setRealName(realName);
                    resList.add(onlineUserDTO);
                }
            });
        }
        //根据时间排序
        Collections.sort(resList, Comparator.comparing(OnlineUserDTO::getLinkTime));
        return LogicalPageUtil.page(resList, current, size);
    }
}
