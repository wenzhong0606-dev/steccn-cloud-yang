package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.platform.domain.IotAlert;
import com.steccn.platform.domain.PlatformUser;
import com.steccn.platform.domain.PlatformUserMac;
import com.steccn.platform.dto.IotAlertDTO;
import com.steccn.platform.service.IotAlertService;
import com.steccn.platform.mapper.IotAlertMapper;
import com.steccn.platform.service.PlatformUserService;
import com.steccn.platform.vo.IotAlertVO;
import com.steccn.platform.vo.PlatformUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【iot_alert(告警表)】的数据库操作Service实现
* @createDate 2025-12-10 14:27:19
*/
@Service
@RequiredArgsConstructor
public class IotAlertServiceImpl extends ServiceImpl<IotAlertMapper, IotAlert>
    implements IotAlertService{
    private final PlatformUserService platformUserService;

    @Override
    public Boolean addAlert(IotAlertDTO dto) {
        IotAlert model = new IotAlert();
        BeanUtil.copyProperties(dto,model);
        save(model);
        return true;
    }

    @Override
    public Boolean updateAlert(IotAlertDTO dto) {
        if(Objects.isNull(dto.getId())){
            throw new BusinessException("数据id不能为空");
        }
        IotAlert model = new IotAlert();
        BeanUtil.copyProperties(dto,model);
        updateById(model);
        return true;
    }

    @Override
    public IotAlertVO getOneInfo(Integer id) {
        IotAlert byId = getById(id);
        IotAlertVO vo = new IotAlertVO();
        BeanUtil.copyProperties(byId,vo);
        List<PlatformUser> platformUsers = platformUserService.listByIds(byId.getReceiver());
        List<String> collect = platformUsers.stream().map(PlatformUser::getName).collect(Collectors.toList());
        vo.setReceiverName(collect);
        return vo;
    }

    @Override
    public Page<IotAlertVO> listInfo(BasePageDTO dto) {
        Page<IotAlert> page = page(new Page<>(dto.getCurrent(), dto.getSize()));
        Page resPage = new Page<IotAlertVO>(dto.getCurrent(), dto.getSize());
        //添加mac地址
        List<IotAlertVO> results = Lists.newArrayList();
        if(CollUtil.isNotEmpty(page.getRecords())){
            Set<Long> collect = page.getRecords().stream().flatMap(v->v.getReceiver().stream()).collect(Collectors.toSet());
            List<PlatformUser> platformUsers = platformUserService.listByIds(collect);
            Map<Long, PlatformUser> userMap = platformUsers.stream()
                    .collect(Collectors.toMap(PlatformUser::getId, u -> u));
            page.getRecords().forEach(tmp->{
                IotAlertVO vo = new IotAlertVO();
                BeanUtil.copyProperties(tmp,vo);
                List<String> userNames = Lists.newArrayList();
                vo.getReceiver().forEach(v->{
                    userNames.add(userMap.get(v).getName());
                });
                vo.setReceiverName(userNames);
                results.add(vo);
            });

        }
        BeanUtils.copyProperties(page, resPage);
        resPage.setRecords(results);
        return resPage;
    }

    @Override
    public Boolean deleteInfo(Integer id) {
        removeById(id);
        return true;
    }
}




