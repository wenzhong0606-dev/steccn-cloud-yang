package com.steccn.ems.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.ems.domain.EpDiscountFactor;
import com.steccn.ems.dto.EpDiscountFactorPageDTO;
import com.steccn.ems.service.EpDiscountFactorService;
import com.steccn.ems.mapper.EpDiscountFactorMapper;
import com.steccn.ems.service.EpEnergyTypeService;
import com.steccn.ems.vo.EpDiscountFactorVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.el.util.ReflectionUtil;
import org.aspectj.util.Reflection;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【ep_discount_factor(折标系数表)】的数据库操作Service实现
 * @createDate 2024-03-08 09:10:12
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EpDiscountFactorServiceImpl extends ServiceImpl<EpDiscountFactorMapper, EpDiscountFactor>
        implements EpDiscountFactorService {

    private final RemoteDictFeignApi remoteDictFeignApi;
    private final EpEnergyTypeService epEnergyTypeService;

    @Override
    public Page<EpDiscountFactorVO> getPage(EpDiscountFactorPageDTO dto) {
        LambdaQueryWrapper<EpDiscountFactor> condition = new LambdaQueryWrapper<EpDiscountFactor>()
                .like(Objects.nonNull(dto.getName()), EpDiscountFactor::getName, dto.getName())
                .eq(Objects.nonNull(dto.getType()), EpDiscountFactor::getType, dto.getType())
                .eq(Objects.nonNull(dto.getEnergyType()), EpDiscountFactor::getEnergyType, dto.getEnergyType());
        //排序
        getSortField(condition, dto.getSortField(), dto.getAscFlag());
        //查询
        Page<EpDiscountFactor> page = page(new Page<>(dto.getCurrent(), dto.getSize()), condition);
        //拷贝数据
        List<EpDiscountFactorVO> resList = BeanUtil.copyToList(page.getRecords(), EpDiscountFactorVO.class);
        //查询数据字典[系数类型，计量单位]，能源类型
        Set<Long> dictSet = Sets.newHashSet();
        Set<Integer> energyIdSet = Sets.newHashSet();
        resList.forEach(ele -> {
            dictSet.add(ele.getType());
            dictSet.add(ele.getValueUnit());
            energyIdSet.add(ele.getEnergyType());
        });
        //去查询数据字典
        Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Integer, String> epEnergyMap = epEnergyTypeService.getBySetIds(energyIdSet);
        resList.forEach(ele -> {
            ele.setTypeName(dictMap.get(ele.getType()));
            ele.setValueUnitName(dictMap.get(ele.getValueUnit()));
            ele.setEnergyTypeName(epEnergyMap.get(ele.getEnergyType()));
        });
        Page<EpDiscountFactorVO> resPage = new Page<>();
        BeanUtil.copyProperties(page, resPage);
        resPage.setRecords(resList);
        return resPage;
    }

    @Override
    public EpDiscountFactorVO info(Integer id) {
        EpDiscountFactor existModel = getById(id);
        Assert.notNull(existModel, "记录不存在");
        EpDiscountFactorVO resVo = BeanUtil.copyProperties(existModel, EpDiscountFactorVO.class);
        //查询数据字典[系数类型，计量单位]，能源类型
        Set<Long> dictSet = Sets.newHashSet();
        Set<Integer> energyIdSet = Sets.newHashSet();
        dictSet.add(existModel.getType());
        dictSet.add(existModel.getValueUnit());
        energyIdSet.add(existModel.getEnergyType());
        //去查询数据字典
        Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Integer, String> epEnergyMap = epEnergyTypeService.getBySetIds(energyIdSet);
        resVo.setTypeName(dictMap.get(resVo.getType()));
        resVo.setValueUnitName(dictMap.get(resVo.getValueUnit()));
        resVo.setEnergyTypeName(epEnergyMap.get(resVo.getEnergyType()));
        return resVo;
    }

    /**
     * 排序
     *
     * @param condition 条件
     * @param sortField 排序字段
     * @param ascFlag   排序-顺序逆序
     */
    private void getSortField(LambdaQueryWrapper<EpDiscountFactor> condition, String sortField, Boolean ascFlag) {
        if ("name".equals(sortField)) {
            condition.orderBy(true, ascFlag, EpDiscountFactor::getName);
        } else if ("type".equals(sortField)) {
            condition.orderBy(true, ascFlag, EpDiscountFactor::getType);
        } else if ("energyType".equals(sortField)) {
            condition.orderBy(true, ascFlag, EpDiscountFactor::getEnergyType);
        } else if ("validStart".equals(sortField)) {
            condition.orderBy(true, ascFlag, EpDiscountFactor::getValidStart);
        }
    }

}




