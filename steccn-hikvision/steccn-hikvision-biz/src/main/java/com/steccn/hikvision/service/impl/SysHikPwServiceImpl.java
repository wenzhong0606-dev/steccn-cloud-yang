package com.steccn.hikvision.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.hikvision.domain.SysHikPw;
import com.steccn.hikvision.dto.SysHikPwDTO;
import com.steccn.hikvision.dto.SysHikPwSaveListDTO;
import com.steccn.hikvision.service.SysHikPwService;
import com.steccn.hikvision.mapper.SysHikPwMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【sys_hik_pw】的数据库操作Service实现
 * @createDate 2023-07-04 15:00:54
 */
@Service
public class SysHikPwServiceImpl extends ServiceImpl<SysHikPwMapper, SysHikPw>
        implements SysHikPwService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pwSave(List<SysHikPwSaveListDTO> sysHikPwDTOList) {
        //获取角色
        Map<String, List<SysHikPwSaveListDTO>> roleMap = sysHikPwDTOList.stream().collect(Collectors.groupingBy(SysHikPwSaveListDTO::getRoleCode));

        roleMap.forEach((k, v) -> {
            Set<Long> codeSet = v.stream().map(SysHikPwSaveListDTO::getIndexCode).collect(Collectors.toSet());
            //1.删除本次不存在的数据
            remove(new LambdaQueryWrapper<SysHikPw>().in(SysHikPw::getIndexCode, codeSet));
            //2.编辑的数据不用考虑
            //3.新增的数据插入
            List<SysHikPwSaveListDTO> insertList = v.stream().filter(item -> Objects.nonNull(item.getIndexCode())).collect(Collectors.toList());
            List<SysHikPw> sysHikPws = BeanUtil.copyToList(insertList, SysHikPw.class);
            saveBatch(sysHikPws);
        });
        return Boolean.TRUE;
    }
}




