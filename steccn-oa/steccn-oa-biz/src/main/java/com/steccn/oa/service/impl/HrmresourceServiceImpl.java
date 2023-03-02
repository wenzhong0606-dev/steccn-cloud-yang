package com.steccn.oa.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.oa.domain.Hrmresource;
import com.steccn.oa.dto.OaUserIdAndName;
import com.steccn.oa.service.HrmresourceService;
import com.steccn.oa.mapper.HrmresourceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【hrmresource】的数据库操作Service实现
 * @createDate 2022-09-01 18:37:02
 */
@Service
@RequiredArgsConstructor
public class HrmresourceServiceImpl extends ServiceImpl<HrmresourceMapper, Hrmresource>
        implements HrmresourceService {

    private final HrmresourceMapper hrmresourceMapper;

    @Override
    public Long getIdByWorkCode(String workCode) {
        Hrmresource hrmresource = this.getOne(new LambdaQueryWrapper<Hrmresource>()
                .eq(Hrmresource::getWorkcode, workCode), false);
        if (Objects.isNull(hrmresource)) {
            return null;
        } else {
            return Long.valueOf(hrmresource.getId());
        }
    }

    @Override
    public OaUserIdAndName getNameByLoginId(String loginId) {
        Hrmresource hrmresource = this.getOne(new LambdaQueryWrapper<Hrmresource>()
                .eq(Hrmresource::getLoginid, loginId), false);
        OaUserIdAndName oaUserIdAndName = new OaUserIdAndName();
        if (!Objects.isNull(hrmresource)) {
            oaUserIdAndName.setName(hrmresource.getLastname());
            oaUserIdAndName.setId(hrmresource.getId());
            return oaUserIdAndName;
        }else {
            return null;
        }

    }

    @Override
    public List<OaUserIdAndName> getUserByName(String userName) {
        List<OaUserIdAndName> userList = Lists.newArrayList();
        List<Hrmresource> list = list(new LambdaQueryWrapper<Hrmresource>().like(Hrmresource::getLastname, userName));
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(hrmresource -> {
                OaUserIdAndName oaUserIdAndName = new OaUserIdAndName();
                oaUserIdAndName.setName(hrmresource.getLastname());
                oaUserIdAndName.setId(hrmresource.getId());
                userList.add(oaUserIdAndName);
            });
        }
        return userList;
    }

    @Override
    public Map<Long, String> listByIdCh(List<Long> idList) {
        Map<Long, String> map = Maps.newHashMap();
        List<Hrmresource> list = list(new LambdaQueryWrapper<Hrmresource>().in(Hrmresource::getId, idList));
        if (CollUtil.isNotEmpty(list)){
            if (CollUtil.isNotEmpty(list)) {
                for (Hrmresource hrmresource : list) {
                    map.put(Long.valueOf(hrmresource.getId()), hrmresource.getLastname());
                }
            }
        }
        return map;
    }
}




