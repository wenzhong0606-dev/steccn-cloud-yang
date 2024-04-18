package com.steccn.ems.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.constant.MpConstant;
import com.steccn.ems.domain.CeFossilFuelSetting;
import com.steccn.ems.domain.CeProduceIngredientSetting;
import com.steccn.ems.dto.CeProduceIngredientSettingDTO;
import com.steccn.ems.dto.CeProduceIngredientSettingPageDTO;
import com.steccn.ems.service.CeProduceIngredientSettingService;
import com.steccn.ems.mapper.CeProduceIngredientSettingMapper;
import com.steccn.ems.vo.CeFossilFuelSettingVO;
import com.steccn.ems.vo.CeProduceIngredientSettingVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

/**
* @author yequan
* @description 针对表【ce_produce_ingredient_setting(生产原料设置表)】的数据库操作Service实现
* @createDate 2024-04-18 09:27:24
*/
@Service
@Slf4j
@RequiredArgsConstructor
public class CeProduceIngredientSettingServiceImpl extends ServiceImpl<CeProduceIngredientSettingMapper, CeProduceIngredientSetting>
    implements CeProduceIngredientSettingService{

    private final Redisson redisson;

    @Override
    public Page<CeProduceIngredientSettingVO> getPage(CeProduceIngredientSettingPageDTO dto) {
        QueryWrapper<CeProduceIngredientSetting> condition = new QueryWrapper<>();
        //排序
        getSortField(condition, dto.getSortField(), dto.getAscFlag());
        //查询
        LambdaQueryWrapper<CeProduceIngredientSetting> lambda = condition.lambda()
                .like(CeProduceIngredientSetting::getMaterialName, dto.getMaterialName());
        Page<CeProduceIngredientSetting> page = page(new Page<>(dto.getCurrent(), dto.getSize()), lambda);
        //拷贝数据
        List<CeProduceIngredientSettingVO> resList = BeanUtil.copyToList(page.getRecords(), CeProduceIngredientSettingVO.class);
        Page<CeProduceIngredientSettingVO> resPage = new Page<>();
        BeanUtil.copyProperties(page, resPage);
        resPage.setRecords(resList);
        return resPage;
    }

    @Override
    public CeProduceIngredientSettingVO info(Integer id) {
        CeProduceIngredientSetting existModel = getById(id);
        Assert.notNull(existModel, "记录不存在");
        CeProduceIngredientSettingVO resVo = BeanUtil.copyProperties(existModel, CeProduceIngredientSettingVO.class);
        return resVo;
    }

    @Override
    public Boolean add(CeProduceIngredientSettingDTO dto) {
        //加分布式锁-反正出现有效期重叠的情况
        RLock lock = redisson.getLock("CE_PRODUCE_INGREDIENT_SETTING");
        lock.lock();
        try {
            //定义model
            CeProduceIngredientSetting model = BeanUtil.copyProperties(dto, CeProduceIngredientSetting.class);
            //有效期不可重叠
            CeProduceIngredientSetting start = getOne(new LambdaQueryWrapper<CeProduceIngredientSetting>()
                    .eq(CeProduceIngredientSetting::getMaterialId, dto.getMaterialId())
                    .not(lr -> lr.gt(CeProduceIngredientSetting::getValidStart, dto.getValidEnd())
                            .or()
                            .lt(CeProduceIngredientSetting::getValidEnd, dto.getValidStart()))
                    .last(MpConstant.STR_LIMIT_1));
            if (Objects.nonNull(start)) {
                throw new BusinessException("与现有生产原料设置【" + start.getMaterialName() + "】有效期有重叠”，无法保存");
            }
            return save(model);
        } catch (Exception e) {
            log.error("生产原料设置 -> add方法报错 {}", e.getCause());
            throw new BusinessException(e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Boolean edit(CeProduceIngredientSettingDTO dto) {
        //加分布式锁-反正出现有效期重叠的情况
        RLock lock = redisson.getLock("CE_PRODUCE_INGREDIENT_SETTING");
        lock.lock();
        try {
            CeProduceIngredientSetting existModel = getById(dto.getId());
            Assert.notNull(existModel, "该记录不存在");
            //定义model
            CeProduceIngredientSetting model = BeanUtil.copyProperties(dto, CeProduceIngredientSetting.class);
            //有效期不可重叠
            CeProduceIngredientSetting start = getOne(new LambdaQueryWrapper<CeProduceIngredientSetting>()
                    .ne(CeProduceIngredientSetting::getId, dto.getId())
                    .eq(CeProduceIngredientSetting::getMaterialId, dto.getMaterialId())
                    .not(lr -> lr.gt(CeProduceIngredientSetting::getValidStart, dto.getValidEnd())
                            .or()
                            .lt(CeProduceIngredientSetting::getValidEnd, dto.getValidStart()))
                    .last(MpConstant.STR_LIMIT_1));
            if (Objects.nonNull(start)) {
                throw new BusinessException("与现有生产原料设置【" + start.getMaterialName() + "】有效期有重叠”，无法保存");
            }
            return updateById(model);
        } catch (Exception e) {
            log.error("生产原料设置 -> edit方法报错 {}", e.getMessage());
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
        if ("materialName".equals(sortField)) {
            condition.orderBy(true, ascFlag, "CONVERT(`material_name` USING gbk)");
        } else if ("validStart".equals(sortField)) {
            condition.orderBy(true, ascFlag, Lists.newArrayList("valid_start", "valid_end"));
        } else {
            condition.orderBy(true, false, "id");
        }
    }
}




