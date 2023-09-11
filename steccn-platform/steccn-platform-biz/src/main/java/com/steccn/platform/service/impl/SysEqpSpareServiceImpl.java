package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.domain.SysEqpSpare;
import com.steccn.platform.dto.EqpSpareDto;
import com.steccn.platform.dto.SpareDTO;
import com.steccn.platform.dto.SparePageDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.mapper.SysEqpSpareMapper;
import com.steccn.platform.service.SysDictService;
import com.steccn.platform.service.SysEqpSpareService;
import com.steccn.platform.vo.EqpSpareVO;
import lombok.RequiredArgsConstructor;
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
public class SysEqpSpareServiceImpl extends ServiceImpl<SysEqpSpareMapper, SysEqpSpare> implements SysEqpSpareService {

    private final SysEqpSpareMapper sysEqpSpareMapper;
    private final SysDictService sysDictService;


    @Override
    public List<EqpSpareVO> listSpares(String name) {
        List<EqpSpareVO> eqpSpareVOList = sysEqpSpareMapper.listSpares(name);
        List<EqpSpareVO> collect = Lists.newArrayList();
        if (CollUtil.isNotEmpty(eqpSpareVOList)) {
            Set<Long> dictIds = Sets.newHashSet();
            eqpSpareVOList.forEach(vo -> {
                dictIds.add(vo.getHighUnitId());
                dictIds.add(vo.getLengthUnitId());
                dictIds.add(vo.getThickUnitId());
                dictIds.add(vo.getVolumeUnitId());
                dictIds.add(vo.getProcurementTypeId());
                dictIds.add(vo.getWeightUnitId());
                dictIds.add(vo.getWideUnitId());
            });
            Map<Long, String> map = sysDictService.listByIdCh(dictIds);
            eqpSpareVOList.forEach(vo -> {
                vo.setHighUnitName(map.get(vo.getHighUnitId()));
                vo.setLengthUnitName(map.get(vo.getLengthUnitId()));
                vo.setThickUnitName(map.get(vo.getThickUnitId()));
                vo.setWeightUnitName(map.get(vo.getWeightUnitId()));
                vo.setWideUnidName(map.get(vo.getWideUnitId()));
                vo.setProcurementTypeName(map.get(vo.getProcurementTypeId()));
                vo.setVolumeUnitName(map.get(vo.getVolumeUnitId()));
            });
            collect = eqpSpareVOList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(EqpSpareVO::getId))), ArrayList::new));
        }
        return collect;
    }

    @Override
    public List<EqpSpareVO> listSpares(String name, String code) {
        //定义筛选条件
        LambdaQueryWrapper<SysEqpSpare> like = new LambdaQueryWrapper<SysEqpSpare>().eq(Objects.nonNull(code) && !code.equals(""), SysEqpSpare::getCode, code).like(Objects.nonNull(name), SysEqpSpare::getName, name);
        List<SysEqpSpare> list = list(like);
        List<EqpSpareVO> eqpSpareVOS = BeanUtil.copyToList(list, EqpSpareVO.class);
        Set<Long> dictIds = Sets.newHashSet();
        eqpSpareVOS.forEach(vo -> {
            dictIds.add(vo.getHighUnitId());
            dictIds.add(vo.getLengthUnitId());
            dictIds.add(vo.getThickUnitId());
            dictIds.add(vo.getVolumeUnitId());
            dictIds.add(vo.getProcurementTypeId());
            dictIds.add(vo.getWeightUnitId());
            dictIds.add(vo.getWideUnitId());
        });
        Map<Long, String> map = sysDictService.listByIdCh(dictIds);
        eqpSpareVOS.forEach(vo -> {
            vo.setHighUnitName(map.get(vo.getHighUnitId()));
            vo.setLengthUnitName(map.get(vo.getLengthUnitId()));
            vo.setThickUnitName(map.get(vo.getThickUnitId()));
            vo.setWeightUnitName(map.get(vo.getWeightUnitId()));
            vo.setWideUnidName(map.get(vo.getWideUnitId()));
            vo.setProcurementTypeName(map.get(vo.getProcurementTypeId()));
            vo.setVolumeUnitName(map.get(vo.getVolumeUnitId()));
        });
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
        Set<Long> dictIds= Sets.newHashSet();
        for (EqpSpareVO eqpSpareVO : eqpSpareVOList) {
            dictIds.add(eqpSpareVO.getHighUnitId());
            dictIds.add(eqpSpareVO.getLengthUnitId());
            dictIds.add(eqpSpareVO.getThickUnitId());
            dictIds.add(eqpSpareVO.getVolumeUnitId());
            dictIds.add(eqpSpareVO.getProcurementTypeId());
            dictIds.add(eqpSpareVO.getWeightUnitId());
            dictIds.add(eqpSpareVO.getWideUnitId());
            resMap.put(eqpSpareVO.getId(), eqpSpareVO);
        }
        Map<Long, String> map = sysDictService.listByIdCh(dictIds);
        eqpSpareVOList.forEach(vo -> {
            vo.setHighUnitName(map.get(vo.getHighUnitId()));
            vo.setLengthUnitName(map.get(vo.getLengthUnitId()));
            vo.setThickUnitName(map.get(vo.getThickUnitId()));
            vo.setWeightUnitName(map.get(vo.getWeightUnitId()));
            vo.setWideUnidName(map.get(vo.getWideUnitId()));
            vo.setProcurementTypeName(map.get(vo.getProcurementTypeId()));
            vo.setVolumeUnitName(map.get(vo.getVolumeUnitId()));
        });
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

    @Override
    public Map<Long, EqpSpareVO> mapByCode(List<String> codeList) {
        Map<Long, EqpSpareVO> resMap = Maps.newHashMap();
        Collection<SysEqpSpare> spareList = baseMapper.listByCode(codeList);
        if (CollUtil.isEmpty(spareList)) {
            return resMap;
        }
        List<EqpSpareVO> spareVOList = BeanUtil.copyToList(spareList, EqpSpareVO.class);
        for (EqpSpareVO spareVO : spareVOList) {
            resMap.put(spareVO.getId(), spareVO);
        }
        return resMap;
    }

    @Override
    public Page<EqpSpareVO> listPage(SparePageDTO sparePageDTO) {
        //1.自定义查询条件
        LambdaQueryWrapper<SysEqpSpare> eq = new LambdaQueryWrapper<SysEqpSpare>()
                .ne(SysEqpSpare::getStatus, OaConstant.NUM_1.toString())
                .like(StrUtil.isNotBlank(sparePageDTO.getName()), SysEqpSpare::getName, sparePageDTO.getName())
                .like(StrUtil.isNotBlank(sparePageDTO.getCode()), SysEqpSpare::getCode, sparePageDTO.getCode())
                .like(StrUtil.isNotBlank(sparePageDTO.getSpecifications()),SysEqpSpare::getSpecifications,sparePageDTO.getSpecifications());
        //2.定义分页对象
        Page page = new Page<>(sparePageDTO.getCurrent(), sparePageDTO.getSize());
        //3.查询分页数据
        IPage resPage = page(page, eq);
        //4.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        //5.拷贝返回的信息
        List<EqpSpareVO> eqpSpareVOList = BeanUtil.copyToList(resPage.getRecords(), EqpSpareVO.class);
        //6.处理数据
        Set<Long> dictIdSet = Sets.newHashSet();
        eqpSpareVOList.forEach(eqpSpareVO -> {
            dictIdSet.add(eqpSpareVO.getProcurementTypeId());
            dictIdSet.add(eqpSpareVO.getUnitId());
            dictIdSet.add(eqpSpareVO.getHighUnitId());
            dictIdSet.add(eqpSpareVO.getLengthUnitId());
            dictIdSet.add(eqpSpareVO.getThickUnitId());
            dictIdSet.add(eqpSpareVO.getVolumeUnitId());
            dictIdSet.add(eqpSpareVO.getWeightUnitId());
            dictIdSet.add(eqpSpareVO.getWideUnitId());
        });
        Map<Long, String> dictMap = sysDictService.listByIdCh(dictIdSet);
        eqpSpareVOList.forEach(eqpSpareVO -> {
            eqpSpareVO.setProcurementTypeName(dictMap.get(eqpSpareVO.getProcurementTypeId()));
            eqpSpareVO.setUnit(dictMap.get(eqpSpareVO.getUnitId()));
            eqpSpareVO.setHighUnitName(dictMap.get(eqpSpareVO.getHighUnitId()));
            eqpSpareVO.setLengthUnitName(dictMap.get(eqpSpareVO.getLengthUnitId()));
            eqpSpareVO.setThickUnitName(dictMap.get(eqpSpareVO.getThickUnitId()));
            eqpSpareVO.setWeightUnitName(dictMap.get(eqpSpareVO.getWeightUnitId()));
            eqpSpareVO.setWideUnidName(dictMap.get(eqpSpareVO.getWideUnitId()));
            eqpSpareVO.setVolumeUnitName(dictMap.get(eqpSpareVO.getVolumeUnitId()));
        });
        return page.setRecords(eqpSpareVOList);
    }

    @Override
    public List<EqpSpareVO> listCondition(SpareDTO spareDTO) {
        List<EqpSpareVO> list = Lists.newArrayList();
        if (Objects.nonNull(spareDTO)) {
            List<SysEqpSpare> sysEqpSpares = list(new LambdaQueryWrapper<SysEqpSpare>().like(StrUtil.isNotBlank(spareDTO.getName()), SysEqpSpare::getName, spareDTO.getName()).like(StrUtil.isNotBlank(spareDTO.getCode()), SysEqpSpare::getCode, spareDTO.getCode()));
            list = BeanUtil.copyToList(sysEqpSpares, EqpSpareVO.class);
        }
        return list;
    }

    @Override
    public List<EqpSpareVO> getByTypeIdSpareList(Set<String> types) {
        List<EqpSpareVO> lists = new ArrayList<>();
        if (types.size() > 0) {
            for (String type : types) {
                List<SysEqpSpare> sysEqpSpares = list(new LambdaQueryWrapper<SysEqpSpare>().eq(SysEqpSpare::getType, type));
                List<EqpSpareVO> list = BeanUtil.copyToList(sysEqpSpares, EqpSpareVO.class);
                lists.addAll(list);
            }
        }
        return lists;
    }

    @Override
    public EqpSpareVO get(Long id) {
        SysEqpSpare spare = getById(id);
        EqpSpareVO eqpSpareVO = new EqpSpareVO();
        BeanUtil.copyProperties(spare, eqpSpareVO);
        return eqpSpareVO;
    }

    @Override
    public EqpSpareVO getByName(String name) {
        SysEqpSpare one = getOne(new LambdaQueryWrapper<SysEqpSpare>().eq(SysEqpSpare::getName, name), false);
        EqpSpareVO vo = new EqpSpareVO();
        if(Objects.nonNull(one)){
            BeanUtil.copyProperties(one, vo);
        }
        return vo;
    }

    @Override
    public Boolean saveSpare(EqpSpareDto eqpSpareDto) {
        SysEqpSpare sysEqpSpare = new SysEqpSpare();
        BeanUtil.copyProperties(eqpSpareDto,sysEqpSpare);
        return save(sysEqpSpare);
    }

    @Override
    public Boolean updateSpare(EqpSpareDto eqpSpareDto) {
        SysEqpSpare sysEqpSpare = new SysEqpSpare();
        BeanUtil.copyProperties(eqpSpareDto,sysEqpSpare);
        return updateById(sysEqpSpare);
    }
}




