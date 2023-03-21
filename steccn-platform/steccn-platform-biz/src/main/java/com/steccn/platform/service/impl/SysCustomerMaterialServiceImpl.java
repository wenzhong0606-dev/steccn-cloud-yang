package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.LockConstant;
import com.steccn.platform.domain.SysCustomerMaterial;
import com.steccn.platform.domain.SysCustomerMaterialInfo;
import com.steccn.platform.dto.SysCustomerMaterialPageDTO;
import com.steccn.platform.dto.SysCustomerMaterialSaveDTO;
import com.steccn.platform.dto.SysCustomerMaterialSubDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.mapper.SysCustomerMaterialMapper;
import com.steccn.platform.service.SysCustomerMaterialInfoService;
import com.steccn.platform.service.SysCustomerMaterialService;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysCustomerMaterialVO;
import com.steccn.pms.vo.PmCraftRouteModuleVO;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【sys_customer_material(客户物料表)】的数据库操作Service实现
 * @createDate 2023-03-03 10:04:02
 */
@Service
@RequiredArgsConstructor
public class SysCustomerMaterialServiceImpl extends ServiceImpl<SysCustomerMaterialMapper, SysCustomerMaterial>
        implements SysCustomerMaterialService {

    private final SysCustomerMaterialInfoService sysCustomerMaterialInfoService;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final TransactionTemplate transactionTemplate;
    private final RedissonClient redissonClient;

    @Override
    public Page<SysCustomerMaterialVO> pageList(SysCustomerMaterialPageDTO dto) {
        Page<SysCustomerMaterialVO> page = this.baseMapper.pageList(new Page<>(dto.getCurrent(), dto.getSize()), dto);
        if (CollUtil.isNotEmpty(page.getRecords())) {
            Set<Long> unitIdSet = page.getRecords().stream().map(SysCustomerMaterialVO::getUnit).collect(Collectors.toSet());
            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(unitIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            page.getRecords().forEach(ele -> {
                ele.setUnitName(dictMap.getOrDefault(ele.getUnit(), Strings.EMPTY));
            });
        }
        return page;
    }

    @Override
    public SysCustomerMaterialVO info(Long id) {
        SysCustomerMaterial sysCustomerMaterial = getById(id);
        Assert.notNull(sysCustomerMaterial, "客户物料不存在");
        List<SysCustomerMaterialInfo> detailList = sysCustomerMaterialInfoService.list(new LambdaQueryWrapper<SysCustomerMaterialInfo>()
                .eq(SysCustomerMaterialInfo::getCmId, id));
        SysCustomerMaterialVO resVo = BeanUtil.copyProperties(sysCustomerMaterial, SysCustomerMaterialVO.class);
        //获取所有unit的数据字典值
        if (CollUtil.isNotEmpty(detailList)) {
            Set<Long> dictIdSet = detailList.stream().map(SysCustomerMaterialInfo::getUnit).collect(Collectors.toSet());
            dictIdSet.add(sysCustomerMaterial.getUnit());
            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            List<SysCustomerMaterialSubDTO> subVOList = Lists.newArrayList();
            detailList.forEach(ele -> {
                SysCustomerMaterialSubDTO subVO = BeanUtil.copyProperties(ele, SysCustomerMaterialSubDTO.class);
                subVO.setUnitName(dictMap.getOrDefault(subVO.getUnit(), Strings.EMPTY));
                subVOList.add(subVO);
            });
            resVo.setUnitName(dictMap.getOrDefault(resVo.getUnit(), Strings.EMPTY));
            resVo.setSubVOList(subVOList);
        }
        return resVo;
    }

    @Override
    public Boolean commit(SysCustomerMaterialSaveDTO dto) {
        //根据客户物料加分布式锁，属于低频操作，无需双重锁判断
        RLock lock = redissonClient.getLock(LockConstant.CUSTOMER_MATERIAL + ":" + dto.getCustomerId() + ":" + dto.getMaterialId());
        lock.lock();
        try {
            if (Objects.nonNull(dto.getId())) {
                SysCustomerMaterial oldModel = getById(dto.getId());
                Assert.notNull(oldModel, "客户物料不存在，无法编辑");
            }
            //防止修改操作，改成物料和客户一致
            long count = count(new LambdaQueryWrapper<SysCustomerMaterial>()
                    .eq(SysCustomerMaterial::getCustomerId, dto.getCustomerId())
                    .eq(SysCustomerMaterial::getMaterialId, dto.getMaterialId())
                    .ne(Objects.nonNull(dto.getId()), SysCustomerMaterial::getId, dto.getId()));
            Assert.isTrue(count == 0, "物料客户已存在，无法新增");
            //如果id不存在，则为新增
            SysCustomerMaterial model = new SysCustomerMaterial();
            BeanUtils.copyProperties(dto, model);
            //根据物料id获取物料信息
            Set<Long> materialIdSet = Sets.newHashSet(dto.getMaterialId());
            Set<Long> detailIdSet = Sets.newHashSet();
            if (CollUtil.isNotEmpty(dto.getSubVOList())) {
                dto.getSubVOList().forEach(ele -> {
                    materialIdSet.add(ele.getMaterialId());
                    if (Objects.nonNull(ele.getId())) {
                        detailIdSet.add(ele.getId());
                    }
                });
            }
            //获取物料信息
            Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(Lists.newArrayList(materialIdSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            EqpSpareVO material = materialMap.get(model.getMaterialId());
            Assert.notNull(material, "物料不存在");
            model.setMaterialCode(material.getCode());
            model.setMaterialName(material.getName());
            //根据客户id获取客户信息
            Map<Integer, String> customerMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(Sets.newHashSet(model.getCustomerId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            model.setCustomerName(customerMap.get(model.getCustomerId()));
            Assert.notNull(model.getCustomerName(), "客户不存在");
            transactionTemplate.execute(status -> {
                //如果id存在，则为编辑
                saveOrUpdate(model);
                //明细逻辑-可能为空
                if (CollUtil.isNotEmpty(dto.getSubVOList())) {
                    //1.编辑逻辑下，删除主数据关联的detailIdSet集合之外的所有明细【极端情况下，明细全部被删除】
                    if (Objects.nonNull(model.getId())) {
                        sysCustomerMaterialInfoService.remove(new LambdaQueryWrapper<SysCustomerMaterialInfo>()
                                .eq(SysCustomerMaterialInfo::getCmId, model.getId())
                                .notIn(CollUtil.isNotEmpty(detailIdSet), SysCustomerMaterialInfo::getId, detailIdSet));
                    }
                    List<SysCustomerMaterialInfo> subList = Lists.newArrayList();
                    dto.getSubVOList().forEach(ele -> {
                        EqpSpareVO subMaterial = materialMap.get(ele.getMaterialId());
                        SysCustomerMaterialInfo subModel = new SysCustomerMaterialInfo();
                        BeanUtils.copyProperties(ele, subModel);
                        subModel.setMaterialCode(subMaterial.getCode());
                        subModel.setMaterialName(subMaterial.getName());
                        subModel.setMaterialSpec(subMaterial.getSpecifications());
                        subModel.setCmId(model.getId());
                        //有id的 属于编辑情况
                        //无id的 属于新增情况
                        subList.add(subModel);
                    });
                    if (CollUtil.isNotEmpty(subList)) {
                        sysCustomerMaterialInfoService.saveOrUpdateBatch(subList);
                    }
                }
                return Boolean.TRUE;
            });
        } finally {
            lock.unlock();
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        removeById(id);
        sysCustomerMaterialInfoService.remove(new LambdaQueryWrapper<SysCustomerMaterialInfo>().eq(SysCustomerMaterialInfo::getCmId, id));
        return Boolean.TRUE;
    }

    @Override
    public SysCustomerMaterialVO customerMaterial(Integer customerId, Long materialId) {
        SysCustomerMaterialVO resVo = null;
        SysCustomerMaterial model = getOne(new LambdaQueryWrapper<SysCustomerMaterial>()
                .eq(SysCustomerMaterial::getCustomerId, customerId)
                .eq(SysCustomerMaterial::getMaterialId, materialId), false);
        if (Objects.nonNull(model)) {
            resVo = BeanUtil.copyProperties(model, SysCustomerMaterialVO.class);
        }
        return resVo;
    }

    @Override
    public List<PmCraftRouteModuleVO> produceOrderMaterial(Integer customerId, Long materialId) {
        List<PmCraftRouteModuleVO> resList = Lists.newArrayList();
        SysCustomerMaterial model = getOne(new LambdaQueryWrapper<SysCustomerMaterial>()
                .eq(SysCustomerMaterial::getCustomerId, customerId)
                .eq(SysCustomerMaterial::getMaterialId, materialId), false);
        if (Objects.nonNull(model)) {
            List<SysCustomerMaterialInfo> infoList = sysCustomerMaterialInfoService.list(new LambdaQueryWrapper<SysCustomerMaterialInfo>().eq(SysCustomerMaterialInfo::getCmId, model.getId()));
            if (CollUtil.isNotEmpty(infoList)) {
                //获取所有数据字典
                Set<Long> unitSet = infoList.stream().map(SysCustomerMaterialInfo::getUnit).collect(Collectors.toSet());
                Map<Long, String> unitMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(unitSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                infoList.forEach(ele -> {
                    PmCraftRouteModuleVO vo = new PmCraftRouteModuleVO();
                    BeanUtils.copyProperties(ele, vo);
                    vo.setUnitId(ele.getUnit());
                    vo.setUnit(unitMap.get(ele.getUnit()));
                    vo.setIsFixedQuantity(ele.getFixedQuantityFlag());
                    vo.setQuantity(ele.getQuantity().intValue());
                    resList.add(vo);
                });
            }
        }
        return resList;
    }
}

