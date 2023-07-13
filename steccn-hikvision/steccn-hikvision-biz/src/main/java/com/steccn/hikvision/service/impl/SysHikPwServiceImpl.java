package com.steccn.hikvision.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.hikvision.domain.SysHikPw;
import com.steccn.hikvision.dto.SysHikPwDTO;
import com.steccn.hikvision.dto.SysHikPwSaveDTO;
import com.steccn.hikvision.dto.SysHikPwSaveListDTO;
import com.steccn.pms.dto.MesRoleDTO;
import com.steccn.hikvision.service.SysHikPwService;
import com.steccn.hikvision.mapper.SysHikPwMapper;
import com.steccn.hikvision.service.SysHikPwService;
import com.steccn.hikvision.vo.CameraVO;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.vo.MessScopeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【sys_hik_pw】的数据库操作Service实现
 * @createDate 2023-07-04 15:00:54
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SysHikPwServiceImpl extends ServiceImpl<SysHikPwMapper, SysHikPw>
        implements SysHikPwService {

    private final RemoteMessFeignApi remoteMessFeignApi;
    private final HikvisionServiceImpl hikvisionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pwSave(List<SysHikPwSaveDTO> dtoList) {

        //对角色的分组
        for (SysHikPwSaveDTO ele : dtoList) {
            int idx = 0;
            List<SysHikPwSaveListDTO> pwList = ele.getPwList();
            //删除该角色的所有权限
            remove(new LambdaQueryWrapper<SysHikPw>().eq(SysHikPw::getRoleCode, ele.getRoleCode()));
            Set<String> codeSet = pwList.stream().map(SysHikPwSaveListDTO::getIndexCode).collect(Collectors.toSet());
            //考虑同一个角色下indexCode是否重复
            if (codeSet.size() != pwList.size()) {
                throw new BusinessException("同一角色下存在重复的视频监控");
            }
            //3.新增的数据插入
            if (CollUtil.isNotEmpty(ele.getPwList())) {
                List<SysHikPw> insertModelList = Lists.newArrayList();
                for (SysHikPwSaveListDTO pwDto : pwList) {
                    SysHikPw model = new SysHikPw();
                    BeanUtils.copyProperties(pwDto, model);
                    model.setSort(idx++);
                    model.setRoleCode(ele.getRoleCode());
                    insertModelList.add(model);
                }
                saveBatch(insertModelList);
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public List<SysHikPwDTO> info(String roleCode) {
        List<SysHikPwDTO> resList = Lists.newArrayList();
        List<SysHikPw> dbList = list(new LambdaQueryWrapper<SysHikPw>().eq(SysHikPw::getRoleCode, roleCode).orderByAsc(SysHikPw::getSort));
        if (Objects.nonNull(dbList)) {
            dbList.forEach(ele -> {
                SysHikPwDTO res = new SysHikPwDTO();
                BeanUtils.copyProperties(ele, res);
                resList.add(res);
            });
        }
        return resList;
    }

    @Override
    public List<CameraVO> tree(String username) {
        //获取用户的角色
        MessScopeVO role = remoteMessFeignApi.getRole(username);
        if (Objects.isNull(role) || CollUtil.isEmpty(role.getStringList())) {
            return Lists.newArrayList();
        }

        //获取数据集
        List<SysHikPw> dbList = list(new LambdaQueryWrapper<SysHikPw>().in(SysHikPw::getRoleCode, role.getStringList()).orderByAsc(SysHikPw::getSort));
        if (CollUtil.isEmpty(dbList)) {
            return Lists.newArrayList();
        }

        //获取所有type = 0 的元素，然后递归去树中查询所有叶子节点
        Set<String> virtualNodeList = dbList.stream().filter(ele -> ele.getType() == 1).map(SysHikPw::getIndexCode).collect(Collectors.toSet());
        Set<CameraVO> resSet = Sets.newHashSet();
        //获取所有的节点
        List<CameraVO> sourceFlatList = hikvisionService.flatCameraList();
        Map<String, Integer> sortMap = Maps.newHashMap();
        for (int i = 0; i < sourceFlatList.size(); i++) {
            sortMap.put(sourceFlatList.get(i).getIndexCode(), i);
        }

        if (CollUtil.isNotEmpty(virtualNodeList) && CollUtil.isNotEmpty(sourceFlatList)) {

            List<CameraVO> filterNodeList = sourceFlatList.stream().filter(ele -> virtualNodeList.contains(ele.getRegionPathName())).collect(Collectors.toList());
            resSet.addAll(filterNodeList);
        }

        Set<String> leafList = dbList.stream().filter(ele -> ele.getType() == 0).map(SysHikPw::getIndexCode).collect(Collectors.toSet());
        if (CollUtil.isNotEmpty(leafList) && CollUtil.isNotEmpty(sourceFlatList)) {
            List<CameraVO> sourceList = sourceFlatList.stream().filter(ele -> leafList.contains(ele.getIndexCode())).collect(Collectors.toList());
            resSet.addAll(sourceList);
        }

        //排序
        List<CameraVO> resList = Lists.newArrayList(resSet);
        resList.forEach(ele -> ele.setSort(sortMap.getOrDefault(ele.getIndexCode(), Integer.MAX_VALUE)));
        resList.sort(Comparator.comparing(CameraVO::getSort));

        //递归成一颗树 返回出去
        return HikvisionServiceImpl.convertToTree(resList);
    }

    @Override
    public List<MesRoleDTO> getAllRole() {
        return remoteMessFeignApi.getAllRole().getMesRoleDTOList();
    }
}




