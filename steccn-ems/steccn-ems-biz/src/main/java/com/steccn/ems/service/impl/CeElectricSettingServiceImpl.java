package com.steccn.ems.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.ems.domain.CeElectricSetting;
import com.steccn.ems.domain.EpDiscountFactor;
import com.steccn.ems.dto.CeElectricSettingDTO;
import com.steccn.ems.service.CeElectricSettingService;
import com.steccn.ems.mapper.CeElectricSettingMapper;
import com.steccn.ems.vo.CeElectricSettingVO;
import com.steccn.ems.vo.EpDiscountFactorVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【ce_electric_setting(电力设置表)】的数据库操作Service实现
 * @createDate 2024-04-18 09:27:24
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CeElectricSettingServiceImpl extends ServiceImpl<CeElectricSettingMapper, CeElectricSetting>
        implements CeElectricSettingService {

    private final Redisson redisson;

    @Override
    public Page<CeElectricSettingVO> getPage(BasePageDTO dto) {
        QueryWrapper<CeElectricSetting> condition = new QueryWrapper<>();
        //排序
        getSortField(condition, dto.getSortField(), dto.getAscFlag());
        //查询
        Page<CeElectricSetting> page = page(new Page<>(dto.getCurrent(), dto.getSize()), condition.lambda());
        //拷贝数据
        List<CeElectricSettingVO> resList = BeanUtil.copyToList(page.getRecords(), CeElectricSettingVO.class);
        Page<CeElectricSettingVO> resPage = new Page<>();
        BeanUtil.copyProperties(page, resPage);
        resPage.setRecords(resList);
        return resPage;
    }

    @Override
    public CeElectricSettingVO info(Integer id) {
        CeElectricSetting existModel = getById(id);
        Assert.notNull(existModel, "记录不存在");
        CeElectricSettingVO resVo = BeanUtil.copyProperties(existModel, CeElectricSettingVO.class);
        return resVo;
    }

    @Override
    public Boolean add(CeElectricSettingDTO dto) {
        //加分布式锁-反正出现有效期重叠的情况
        RLock lock = redisson.getLock("CE_ELECTRIC_SETTING");
        lock.lock();
        try {
            //定义model
            CeElectricSetting model = BeanUtil.copyProperties(dto, CeElectricSetting.class);
            //有效期不可重叠
            CeElectricSetting start = getOne(new LambdaQueryWrapper<CeElectricSetting>()
                    .not(lr -> lr.gt(CeElectricSetting::getValidStart, dto.getValidEnd())
                            .or()
                            .lt(CeElectricSetting::getValidEnd, dto.getValidStart()))
                    .last(MpConstant.STR_LIMIT_1));
            if (Objects.nonNull(start)) {
                throw new BusinessException("与现有电力设置【" + start.getName() + "】有效期有重叠”，无法保存");
            }
            return save(model);
        } catch (Exception e) {
            log.error("电力设置 -> add方法报错 {}", e.getCause());
            throw new BusinessException(e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Boolean edit(CeElectricSettingDTO dto) {
        //加分布式锁-反正出现有效期重叠的情况
        RLock lock = redisson.getLock("CE_ELECTRIC_SETTING");
        lock.lock();
        try {
            CeElectricSetting existModel = getById(dto.getId());
            Assert.notNull(existModel, "该记录不存在");
            //定义model
            CeElectricSetting model = BeanUtil.copyProperties(dto, CeElectricSetting.class);
            //有效期不可重叠
            CeElectricSetting start = getOne(new LambdaQueryWrapper<CeElectricSetting>()
                    .ne(CeElectricSetting::getId, dto.getId())
                    .not(lr -> lr.gt(CeElectricSetting::getValidStart, dto.getValidEnd())
                            .or()
                            .lt(CeElectricSetting::getValidEnd, dto.getValidStart()))
                    .last(MpConstant.STR_LIMIT_1));
            if (Objects.nonNull(start)) {
                throw new BusinessException("与现有电力设置【" + start.getName() + "】有效期有重叠”，无法保存");
            }
            return updateById(model);
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            throw new BusinessException(e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    /**
     * 排序
     *
     * @param condition 条件
     * @param sortField 排序字段
     * @param ascFlag   排序-顺序逆序
     */
    private void getSortField(QueryWrapper condition, String sortField, Boolean ascFlag) {
        if ("validStart".equals(sortField)) {
            condition.orderBy(true, ascFlag, Lists.newArrayList("valid_start", "valid_end"));
        } else {
            condition.orderBy(true, false, "id");
        }
    }
}