package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.platform.domain.SysDict;
import com.steccn.platform.dto.SpareDTO;
import com.steccn.platform.domain.SysEqpSpare;
import com.steccn.platform.service.SysEqpSpareService;
import com.steccn.platform.mapper.SysEqpSpareMapper;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author W10
 * @description 针对表【sys_eqp_spare(Mes备品备件表)】的数据库操作Service实现
 * @createDate 2022-09-02 16:22:51
 */
@Service
@RequiredArgsConstructor
public class SysEqpSpareServiceImpl extends ServiceImpl<SysEqpSpareMapper, SysEqpSpare>
        implements SysEqpSpareService {

    private final SysEqpSpareMapper sysEqpSpareMapper;

    @Override
    public List<EqpSpareVO> listSpares(String name) {
        List<EqpSpareVO> eqpSpareVOList = sysEqpSpareMapper.listSpares(name);
        List<EqpSpareVO> collect = Lists.newArrayList();
        if (CollUtil.isNotEmpty(eqpSpareVOList)) {
            collect = eqpSpareVOList.stream().collect(Collectors.collectingAndThen(
                    Collectors.toCollection(() -> new TreeSet<>(
                            Comparator.comparing(EqpSpareVO::getId))), ArrayList::new));
        }
        return collect;
    }

    @Override
    public List<EqpSpareVO> listSpares(String name, String code) {
        //定义筛选条件
        LambdaQueryWrapper<SysEqpSpare> like = new LambdaQueryWrapper<SysEqpSpare>().eq(Objects.nonNull(code) && !code.equals(""), SysEqpSpare::getCode, code)
                .like(Objects.nonNull(name), SysEqpSpare::getName, name);
        List<SysEqpSpare> list = list(like);
        List<EqpSpareVO> eqpSpareVOS = BeanUtil.copyToList(list, EqpSpareVO.class);
        return eqpSpareVOS;
    }

    @Override
    public Map<Long, EqpSpareVO> searchSpares(String name, String code) {
        Map<Long, EqpSpareVO> resMap = Maps.newHashMap();
        List<EqpSpareVO> eqpSpareVOS = listSpares(name, code);
        if (CollUtil.isEmpty(eqpSpareVOS)) {
            return resMap;
        }

        for (EqpSpareVO eqpSpareVO : eqpSpareVOS) {
            resMap.put(eqpSpareVO.getId(), eqpSpareVO);
        }
        return resMap;
    }

    @Override
    public EqpSpareVO getByCode(String code) {
        return sysEqpSpareMapper.getByCode(code);
    }

    @Override
    public Map<String, EqpSpareVO> listByCode(List<String> codeList) {
        Map<String, EqpSpareVO> resMap = Maps.newHashMap();
        Collection<SysEqpSpare> spareList = baseMapper.listByCode(codeList);
        if (CollUtil.isEmpty(spareList)) {
            return resMap;
        }
        List<EqpSpareVO> spareVOList = BeanUtil.copyToList(spareList, EqpSpareVO.class);
        for (EqpSpareVO spareVO : spareVOList) {
            resMap.put(spareVO.getCode(), spareVO);
        }
        return resMap;
    }

    @Override
    public Map<Long, EqpSpareVO> listById(List<Long> materialIds) {
        Map<Long, EqpSpareVO> resMap = Maps.newHashMap();
        Collection<SysEqpSpare> spareList = listByIds(materialIds);
        if (CollUtil.isEmpty(spareList)) {
            return resMap;
        }
        List<EqpSpareVO> eqpSpareVOList = BeanUtil.copyToList(spareList, EqpSpareVO.class);
        for (EqpSpareVO eqpSpareVO : eqpSpareVOList) {
            resMap.put(eqpSpareVO.getId(), eqpSpareVO);
        }
        return resMap;
    }

    @Override
    public Map<Long, String> listByIdCh(Set<Long> materialIds) {
        Map<Long, String> resMap = Maps.newHashMap();
        Collection<SysEqpSpare> spareList = listByIds(materialIds);
        if (CollUtil.isEmpty(spareList)) {
            return resMap;
        }
        List<EqpSpareVO> eqpSpareVOList = BeanUtil.copyToList(spareList, EqpSpareVO.class);
        for (EqpSpareVO eqpSpareVO : eqpSpareVOList) {
            resMap.put(eqpSpareVO.getId(), eqpSpareVO.getName());
        }
        return resMap;
    }
}




