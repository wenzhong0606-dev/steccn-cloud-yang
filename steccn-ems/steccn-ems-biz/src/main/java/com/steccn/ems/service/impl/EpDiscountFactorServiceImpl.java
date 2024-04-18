package com.steccn.ems.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.ems.domain.EpDiscountFactor;
import com.steccn.ems.dto.EpDiscountFactorDTO;
import com.steccn.ems.dto.EpDiscountFactorPageDTO;
import com.steccn.ems.service.EpDiscountFactorService;
import com.steccn.ems.mapper.EpDiscountFactorMapper;
import com.steccn.ems.service.EpEnergyTypeService;
import com.steccn.ems.vo.EpDiscountFactorVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.el.util.ReflectionUtil;
import org.aspectj.util.Reflection;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
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
    private final Redisson redisson;
    private static final String EP_DISCOUNT_FACTOR_PREFIX = "ZBXS";

    @Override
    public Page<EpDiscountFactorVO> getPage(EpDiscountFactorPageDTO dto) {
        QueryWrapper<EpDiscountFactor> condition = new QueryWrapper<EpDiscountFactor>();
        //排序
        getSortField(condition, dto.getSortField(), dto.getAscFlag());
        LambdaQueryWrapper<EpDiscountFactor> eq = condition.lambda()
                .like(Objects.nonNull(dto.getName()), EpDiscountFactor::getName, dto.getName())
                .eq(Objects.nonNull(dto.getType()), EpDiscountFactor::getType, dto.getType())
                .eq(Objects.nonNull(dto.getEnergyType()), EpDiscountFactor::getEnergyType, dto.getEnergyType());
        //查询
        Page<EpDiscountFactor> page = page(new Page<>(dto.getCurrent(), dto.getSize()), eq);
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

    @Override
    public Boolean add(EpDiscountFactorDTO dto) {
        //加分布式锁-用于生成唯一主键 格式：ZBXS#6位数字#
        RLock lock = redisson.getLock("EP_DISCOUNT_FACTOR");
        lock.lock();
        try {
            //系数名称不可重复
            long count = count(new LambdaQueryWrapper<EpDiscountFactor>().eq(EpDiscountFactor::getName, dto.getName()));
            Assert.isTrue(count == 0, "系数名称不可重复");
            //定义model
            EpDiscountFactor model = BeanUtil.copyProperties(dto, EpDiscountFactor.class);
            //生成唯一编码
            List<EpDiscountFactor> codeList = list(new LambdaQueryWrapper<EpDiscountFactor>()
                    .likeRight(EpDiscountFactor::getCode, EP_DISCOUNT_FACTOR_PREFIX)
                    .orderByDesc(EpDiscountFactor::getCode).last(MpConstant.STR_LIMIT_1));
            Integer codeInt = 1;
            String nextCode;
            if (CollUtil.isNotEmpty(codeList)) {
                EpDiscountFactor maxCodeModel = codeList.get(0);
                String code = maxCodeModel.getCode();
                String substring = code.substring(EP_DISCOUNT_FACTOR_PREFIX.length());
                codeInt = Integer.parseInt(substring);
                codeInt += 1;
            }
            String codeIntStr = String.valueOf(codeInt);
            nextCode = StringUtils.leftPad(codeIntStr, 6, "0");
            model.setCode(EP_DISCOUNT_FACTOR_PREFIX + nextCode);
            //有效期不可重叠
            EpDiscountFactor start = getOne(new LambdaQueryWrapper<EpDiscountFactor>()
                    .eq(EpDiscountFactor::getType, dto.getType())
                    .eq(Objects.nonNull(dto.getEnergyType()), EpDiscountFactor::getEnergyType, dto.getEnergyType())
                    .not(lr -> lr.gt(EpDiscountFactor::getValidStart, dto.getValidEnd())
                            .or()
                            .lt(EpDiscountFactor::getValidEnd, dto.getValidStart()))
                    .last(MpConstant.STR_LIMIT_1));
            if (Objects.nonNull(start)) {
                throw new BusinessException("与现有系数【" + start.getName() + "】有效期有重叠”，无法保存");
            }
            save(model);
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            throw new BusinessException(e.getMessage());
        } finally {
            lock.unlock();
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean edit(EpDiscountFactorDTO dto) {
        //加分布式锁-用于生成唯一主键 格式：ZBXS#6位数字#
        RLock lock = redisson.getLock("EP_DISCOUNT_FACTOR");
        lock.lock();
        try {
            EpDiscountFactor existModel = getById(dto.getId());
            Assert.notNull(existModel, "该记录不存在");
            //系数名称不可重复
            long count = count(new LambdaQueryWrapper<EpDiscountFactor>()
                    .ne(EpDiscountFactor::getId, dto.getId())
                    .eq(EpDiscountFactor::getName, dto.getName()));
            Assert.isTrue(count == 0, "系数名称不可重复");
            //定义model
            EpDiscountFactor model = BeanUtil.copyProperties(dto, EpDiscountFactor.class);
            //编码不可更改
            model.setCode(existModel.getCode());
            //有效期不可重叠
            EpDiscountFactor start = getOne(new LambdaQueryWrapper<EpDiscountFactor>()
                    .eq(EpDiscountFactor::getType, dto.getType())
                    .ne(EpDiscountFactor::getId, dto.getId())
                    .eq(Objects.nonNull(dto.getEnergyType()), EpDiscountFactor::getEnergyType, dto.getEnergyType())
                    .not(lr -> lr.gt(EpDiscountFactor::getValidStart, dto.getValidEnd())
                            .or()
                            .lt(EpDiscountFactor::getValidEnd, dto.getValidStart()))
                    .last(MpConstant.STR_LIMIT_1));
            if (Objects.nonNull(start)) {
                throw new BusinessException("与现有系数【" + start.getName() + "】有效期有重叠”，无法保存");
            }
            updateById(model);
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            throw new BusinessException(e.getMessage());
        } finally {
            lock.unlock();
        }
        return Boolean.TRUE;

    }

    @Override
    public List<EpDiscountFactorVO> getValidDiscountList(Long type, LocalDate start, LocalDate end) {
        //开始时间 <= start
        //结束时间 >= end
        List<EpDiscountFactor> dbList = list(new LambdaQueryWrapper<EpDiscountFactor>()
                .eq(EpDiscountFactor::getType, type)
                .le(EpDiscountFactor::getValidStart, start)
                .ge(EpDiscountFactor::getValidEnd, end));
        List<EpDiscountFactorVO> resList = BeanUtil.copyToList(dbList, EpDiscountFactorVO.class);
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
        return resList;
    }

    /**
     * 排序
     *
     * @param condition 条件
     * @param sortField 排序字段
     * @param ascFlag   排序-顺序逆序
     */
    private void getSortField(QueryWrapper condition, String sortField, Boolean ascFlag) {
        if ("name".equals(sortField)) {
            condition.orderBy(true, ascFlag, "CONVERT(`name` USING gbk)");
        } else if ("type".equals(sortField)) {
            condition.orderBy(true, ascFlag, "type");
        } else if ("energyType".equals(sortField)) {
            condition.orderBy(true, ascFlag, "energy_type");
        } else if ("validStart".equals(sortField)) {
            condition.orderBy(true, ascFlag, Lists.newArrayList("valid_start", "valid_end"));
        } else {
            condition.orderBy(true, false, "id");
        }
    }

}




