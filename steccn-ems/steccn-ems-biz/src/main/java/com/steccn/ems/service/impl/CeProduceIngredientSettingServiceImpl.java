package com.steccn.ems.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.GetValueUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.CeElectricSetting;
import com.steccn.ems.domain.CeFossilFuelSetting;
import com.steccn.ems.domain.CeProduceIngredientSetting;
import com.steccn.ems.dto.CeProduceIngredientSettingDTO;
import com.steccn.ems.dto.CeProduceIngredientSettingPageDTO;
import com.steccn.ems.service.CeProduceIngredientSettingService;
import com.steccn.ems.mapper.CeProduceIngredientSettingMapper;
import com.steccn.ems.vo.CeFossilFuelSettingVO;
import com.steccn.ems.vo.CeProduceIngredientSettingVO;
import com.steccn.wms.feign.RemoteWmsStorageReceiptFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
    private final RemoteWmsStorageReceiptFeignApi remoteWmsStorageReceiptFeignApi;

    @Override
    public Page<CeProduceIngredientSettingVO> getPage(CeProduceIngredientSettingPageDTO dto) {
        QueryWrapper<CeProduceIngredientSetting> condition = new QueryWrapper<>();
        //排序
        getSortField(condition, dto.getSortField(), dto.getAscFlag());
        //查询
        LambdaQueryWrapper<CeProduceIngredientSetting> lambda = condition.lambda()
                .like(StrUtil.isNotBlank(dto.getMaterialName()), CeProduceIngredientSetting::getMaterialName, dto.getMaterialName());
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

    @Override
    public List<TupleDTO<String,Map<LocalDate, BigDecimal>>> carbonSumReportDay(LocalDate start, LocalDate end) {
        List<TupleDTO<String,Map<LocalDate, BigDecimal>>> resList = Lists.newArrayList();
        /**
         * 生产原料：日期D原料P的碳排放量C = 当日P的消耗量 * P的排放因子 * P的纯度 * P的煅烧比例 / 100 / 100
         *
         *   当日P的消耗量
         *      P的“用量统计来源”为“抄表记录”时：暂无此情况
         *           “用量统计来源”属性：参见生产原料设置
         *      P的“用量统计来源”为“领料记录”（例如纯碱）时：当日P的消耗量为 当日P的生产领料总吨数 - 当日P的生产退料总吨数
         *           统计当日P的生产领料/生产退料总吨数时，查询满足下列条件的出库/入库单
         *              类型为“生产领料”/“生产退料”
         *              状态为“已确认”
         *              确认时间在区间(D 8:00:00, D+1 8:00:00]内
         *              明细中包含P
         *           若查询不到满足条件的出库/入库单，C计为空值
         * 排放因子、纯度、煅烧比例取自当日P对应有效期的设置，若取不到值C计为空值（例如P的设置均已过期）
         * 小数点后最多2位
         */

        //先获取有效期内的生产原料
        List<CeProduceIngredientSetting> validList = list(new LambdaQueryWrapper<CeProduceIngredientSetting>()
                .not(lr -> lr.gt(CeProduceIngredientSetting::getValidStart, end)
                        .or()
                        .lt(CeProduceIngredientSetting::getValidEnd, start)));
        if(CollUtil.isNotEmpty(validList)){
            //将id转换成名称
            LinkedHashMap<Long, String> linkedHashMap = Maps.newLinkedHashMap();
            validList.forEach(ele -> {
                linkedHashMap.put(ele.getMaterialId(), ele.getMaterialName());
            });
            Map<Long,Map<LocalDate, BigDecimal>> consumeMap = Maps.newHashMap();
            //这里考虑性能-优先考虑异步
            for (Long materialId : linkedHashMap.keySet()) {
                Map<LocalDate, BigDecimal> valueMap = FeignOptional.of(() -> remoteWmsStorageReceiptFeignApi.produceConsumeQuantity(new TripleDTO<>(materialId, start, end)), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
                consumeMap.put(materialId, valueMap);
            }
            //开始遍历-形成返回集合
            LocalDate startTmp = start;
            Map<Long,Map<LocalDate, BigDecimal>> resMap = Maps.newHashMap();
            while (LocalDateUtil.le(startTmp, end)) {
                LocalDate finalValidStart = startTmp;
                List<CeProduceIngredientSetting> settingList = validList.stream()
                        .filter(ele -> EpManagerConstant.CarbonCountSourceEnum.PICK_MATERIAL.getCode().equals(ele.getCountSource()))
                        .filter(ele -> LocalDateUtil.isRange(finalValidStart, ele.getValidStart(), ele.getValidEnd(), true, true))
                        .collect(Collectors.toList());
                if (CollUtil.isNotEmpty(settingList)) {
                    for (CeProduceIngredientSetting setting : settingList) {
                        Map<LocalDate, BigDecimal> valueMap = consumeMap.get(setting.getMaterialId());
                        BigDecimal consumeValue = valueMap.get(finalValidStart);
                        BigDecimal value = BigDecimalUtil.multiply(consumeValue, setting.getFactorValue());
                        value = BigDecimalUtil.multiply(value, setting.getPurityValue());
                        value = BigDecimalUtil.multiply(value, setting.getCalcinationRatio());
                        value = BigDecimalUtil.divide(value, BigDecimalUtil.valueOf("10000"));
                        if(Objects.nonNull(value)){
                            resMap.computeIfAbsent(setting.getMaterialId(), x -> Maps.newHashMap()).put(finalValidStart, value);
                        }
                    }
                }
                startTmp = startTmp.plusDays(1);
            }
            linkedHashMap.forEach((k,v) ->{
                resList.add(new TupleDTO<>(v, resMap.getOrDefault(k, new HashMap<>())));

            });
        }
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
        if ("materialName".equals(sortField)) {
            condition.orderBy(true, ascFlag, "CONVERT(`material_name` USING gbk)");
        } else if ("validStart".equals(sortField)) {
            condition.orderBy(true, ascFlag, Lists.newArrayList("valid_start", "valid_end"));
        } else {
            condition.orderBy(true, false, "id");
        }
    }
}




