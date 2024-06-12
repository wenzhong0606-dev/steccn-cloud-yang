package com.steccn.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.mes.feign.RemoteMesFeignApi;
import com.steccn.platform.domain.SysRouteRecord;
import com.steccn.platform.dto.SysRouteRecordDTO;
import com.steccn.platform.service.SysRouteRecordService;
import com.steccn.platform.mapper.SysRouteRecordMapper;
import com.steccn.platform.vo.SysRouteRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Admin
* @description 针对表【sys_route_record(路由跳转记录表)】的数据库操作Service实现
* @createDate 2023-04-19 11:18:38
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class SysRouteRecordServiceImpl extends ServiceImpl<SysRouteRecordMapper, SysRouteRecord>
    implements SysRouteRecordService{
    private final RemoteMesFeignApi remoteMesFeignApi;

    @Override
    public Boolean saveRecord(SysRouteRecordDTO dto) {
        SysRouteRecord model = new SysRouteRecord();
        model.setUserid(SecurityUtils.getUser().getUserId());
        JSONObject jsonObject = new JSONObject(dto.getRecordData());
        model.setRouteData(jsonObject.toString());
        return save(model);
    }

    @Override
    public List<SysRouteRecordVO> getRouteRecord() {
        QueryWrapper<SysRouteRecord> condition = new QueryWrapper<>();
        condition.select("JSON_EXTRACT(route_data, '$.id') AS unique_id,MAX(create_time) AS create_time,MAX(route_data) AS route_data")
                .eq("userId",SecurityUtils.getUser().getUserId())
                .groupBy("unique_id")
                .orderByDesc("create_time")
                .last("LIMIT 5");
        List<SysRouteRecord> list = list(condition);
        List<SysRouteRecordVO> results = Lists.newArrayList();
        if(CollUtil.isNotEmpty(list)){
            List<Long> menuIds = FeignOptional.of(() -> remoteMesFeignApi.getUserMenuAuth(SecurityUtils.getUser().getUsername()), FeignConstant.STECCN_MES_SERVICE_NAME).get();
            list.forEach(tmp->{
                SysRouteRecordVO result = new SysRouteRecordVO();
                String routeData = tmp.getRouteData().toString();
                Long menuId = com.alibaba.fastjson.JSONObject.parseObject(routeData).getLong("id");
                if(menuIds.contains(menuId)){
                    result.setRouteData(routeData);
                    results.add(result);
                }
            });
        }
        return results;
    }
}




