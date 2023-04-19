package com.steccn.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.platform.domain.SysRouteRecord;
import com.steccn.platform.dto.SysRouteRecordDTO;
import com.steccn.platform.service.SysRouteRecordService;
import com.steccn.platform.mapper.SysRouteRecordMapper;
import com.steccn.platform.vo.SysRouteRecordVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Admin
* @description 针对表【sys_route_record(路由跳转记录表)】的数据库操作Service实现
* @createDate 2023-04-19 11:18:38
*/
@Service
public class SysRouteRecordServiceImpl extends ServiceImpl<SysRouteRecordMapper, SysRouteRecord>
    implements SysRouteRecordService{

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
        LambdaQueryWrapper<SysRouteRecord> condition = new LambdaQueryWrapper<>();
        condition.select(SysRouteRecord::getRouteData)
                .eq(SysRouteRecord::getUserid,SecurityUtils.getUser().getUserId())
                .orderByDesc(SysRouteRecord::getCreateTime)
                .last("LIMIT 4");
        List<SysRouteRecord> list = list(condition);
        List<SysRouteRecordVO> results = Lists.newArrayList();
        if(CollUtil.isNotEmpty(list)){
            list.forEach(tmp->{
                SysRouteRecordVO result = new SysRouteRecordVO();
                result.setRouteData(tmp.getRouteData().toString());
                results.add(result);
            });
        }
        return results;
    }
}




