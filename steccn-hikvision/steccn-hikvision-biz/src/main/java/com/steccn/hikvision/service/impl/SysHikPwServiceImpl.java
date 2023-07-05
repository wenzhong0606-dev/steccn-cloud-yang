package com.steccn.hikvision.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.hikvision.domain.SysHikPw;
import com.steccn.hikvision.dto.SysHikPwDTO;
import com.steccn.hikvision.dto.SysHikPwSaveDTO;
import com.steccn.hikvision.dto.SysHikPwSaveListDTO;
import com.steccn.hikvision.service.SysHikPwService;
import com.steccn.hikvision.mapper.SysHikPwMapper;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SysHikPwServiceImpl extends ServiceImpl<SysHikPwMapper, SysHikPw>
        implements SysHikPwService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pwSave(List<SysHikPwSaveDTO> dtoList) {
        //对角色的分组
        for (SysHikPwSaveDTO ele : dtoList) {
            List<SysHikPwSaveListDTO> v = ele.getPwList();
            if (CollUtil.isEmpty(v)) {
                //删除该角色的所有权限
                remove(new LambdaQueryWrapper<SysHikPw>().eq(SysHikPw::getRoleCode, ele.getRoleCode()));
            } else {
                Set<String> codeSet = v.stream().map(SysHikPwSaveListDTO::getIndexCode).collect(Collectors.toSet());
                Set<Integer> existIdSet = v.stream().filter(e -> Objects.nonNull(e.getId())).map(SysHikPwSaveListDTO::getId).collect(Collectors.toSet());
                //考虑同一个角色下indexCode是否重复
                if (codeSet.size() != v.size()) {
                    throw new BusinessException("同一角色下存在重复的视频监控");
                }
                //1.删除本次不存在的数据，如果indexCode为空，则删除所有
                remove(new LambdaQueryWrapper<SysHikPw>()
                        .eq(SysHikPw::getRoleCode, ele.getRoleCode())
                        .notIn(CollUtil.isNotEmpty(existIdSet), SysHikPw::getId, existIdSet));
                //2.编辑的数据考虑覆盖
                List<SysHikPwSaveListDTO> editList = v.stream().filter(item -> Objects.nonNull(item.getId())).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(editList)) {
                    List<SysHikPw> editModelList = BeanUtil.copyToList(editList, SysHikPw.class);
                    editModelList.forEach(aa -> aa.setRoleCode(ele.getRoleCode()));
                    updateBatchById(editModelList);
                }
                //3.新增的数据插入
                List<SysHikPwSaveListDTO> insertList = v.stream().filter(item -> Objects.isNull(item.getId())).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(insertList)) {
                    List<SysHikPw> insertModelList = BeanUtil.copyToList(insertList, SysHikPw.class);
                    insertModelList.forEach(bb -> bb.setRoleCode(ele.getRoleCode()));
                    saveBatch(insertModelList);
                }
            }
        }
        return Boolean.TRUE;
    }
}




