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
import com.steccn.common.util.LocalDateUtil;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.CeElectricSetting;
import com.steccn.ems.domain.CeFossilFuelSetting;
import com.steccn.ems.domain.CeProduceIngredientSetting;
import com.steccn.ems.dto.CeFossilFuelSettingDTO;
import com.steccn.ems.dto.CeFossilFuelSettingPageDTO;
import com.steccn.ems.service.CeFossilFuelSettingService;
import com.steccn.ems.mapper.CeFossilFuelSettingMapper;
import com.steccn.ems.service.EpDayCountReportService;
import com.steccn.ems.vo.CeElectricSettingVO;
import com.steccn.ems.vo.CeFossilFuelSettingVO;
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
 * @description 针对表【ce_fossil_fuel_setting(化石燃料设置表)】的数据库操作Service实现
 * @createDate 2024-04-18 09:27:24
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CeFossilFuelSettingServiceImpl extends ServiceImpl<CeFossilFuelSettingMapper, CeFossilFuelSetting>
        implements CeFossilFuelSettingService {

    private final Redisson redisson;
    private final RemoteWmsStorageReceiptFeignApi remoteWmsStorageReceiptFeignApi;
    private final EpDayCountReportService epDayCountReportService;


    @Override
    public Page<CeFossilFuelSettingVO> getPage(CeFossilFuelSettingPageDTO dto) {
        QueryWrapper<CeFossilFuelSetting> condition = new QueryWrapper<>();
        //排序
        getSortField(condition, dto.getSortField(), dto.getAscFlag());
        //查询
        LambdaQueryWrapper<CeFossilFuelSetting> lambda = condition.lambda()
                .like(StrUtil.isNotBlank(dto.getMaterialName()), CeFossilFuelSetting::getMaterialName, dto.getMaterialName());
        Page<CeFossilFuelSetting> page = page(new Page<>(dto.getCurrent(), dto.getSize()), lambda);
        //拷贝数据
        List<CeFossilFuelSettingVO> resList = BeanUtil.copyToList(page.getRecords(), CeFossilFuelSettingVO.class);
        Page<CeFossilFuelSettingVO> resPage = new Page<>();
        BeanUtil.copyProperties(page, resPage);
        resPage.setRecords(resList);
        return resPage;
    }

    @Override
    public CeFossilFuelSettingVO info(Integer id) {
        CeFossilFuelSetting existModel = getById(id);
        Assert.notNull(existModel, "记录不存在");
        CeFossilFuelSettingVO resVo = BeanUtil.copyProperties(existModel, CeFossilFuelSettingVO.class);
        return resVo;
    }

    @Override
    public Boolean add(CeFossilFuelSettingDTO dto) {
        //加分布式锁-反正出现有效期重叠的情况
        RLock lock = redisson.getLock("CE_FOSSIL_FUEL_SETTING");
        lock.lock();
        try {
            //定义model
            CeFossilFuelSetting model = BeanUtil.copyProperties(dto, CeFossilFuelSetting.class);
            //有效期不可重叠
            CeFossilFuelSetting start = getOne(new LambdaQueryWrapper<CeFossilFuelSetting>()
                    .eq(CeFossilFuelSetting::getMaterialId, dto.getMaterialId())
                    .not(lr -> lr.gt(CeFossilFuelSetting::getValidStart, dto.getValidEnd())
                            .or()
                            .lt(CeFossilFuelSetting::getValidEnd, dto.getValidStart()))
                    .last(MpConstant.STR_LIMIT_1));
            if (Objects.nonNull(start)) {
                throw new BusinessException("与现有化石燃料设置【" + start.getMaterialName() + "】有效期有重叠”，无法保存");
            }
            return save(model);
        } catch (Exception e) {
            log.error("化石燃料设置 -> add方法报错 {}", e.getCause());
            throw new BusinessException(e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Boolean edit(CeFossilFuelSettingDTO dto) {
        //加分布式锁-反正出现有效期重叠的情况
        RLock lock = redisson.getLock("CE_FOSSIL_FUEL_SETTING");
        lock.lock();
        try {
            CeFossilFuelSetting existModel = getById(dto.getId());
            Assert.notNull(existModel, "该记录不存在");
            //定义model
            CeFossilFuelSetting model = BeanUtil.copyProperties(dto, CeFossilFuelSetting.class);
            //有效期不可重叠
            CeFossilFuelSetting start = getOne(new LambdaQueryWrapper<CeFossilFuelSetting>()
                    .ne(CeFossilFuelSetting::getId, dto.getId())
                    .eq(CeFossilFuelSetting::getMaterialId, dto.getMaterialId())
                    .not(lr -> lr.gt(CeFossilFuelSetting::getValidStart, dto.getValidEnd())
                            .or()
                            .lt(CeFossilFuelSetting::getValidEnd, dto.getValidStart()))
                    .last(MpConstant.STR_LIMIT_1));
            if (Objects.nonNull(start)) {
                throw new BusinessException("与现有化石燃料设置【" + start.getMaterialName() + "】有效期有重叠”，无法保存");
            }
            return updateById(model);
        } catch (Exception e) {
            log.error("化石燃料设置 -> edit方法报错 {}", e.getMessage());
            throw new BusinessException(e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<TupleDTO<String, Map<LocalDate, BigDecimal>>> carbonSumReportDay(LocalDate start, LocalDate end) {
        List<TupleDTO<String, Map<LocalDate, BigDecimal>>> resList = Lists.newArrayList();
        /**
         * 化石燃料：日期D燃料F的碳排放量C = 当日F的消耗量 * F的低位发热量 * F的单位热值含碳量 * F的碳氧化率 / 100 * 44 / 12
         *
         *   当日F的消耗量
         *     F的“用量统计来源”为“抄表记录”（例如燃料油、天然气）时：当日F的消耗量取自【能耗日统计报表】中F的消耗值（转化为吨/万m³），若取不到值C计为空值（例如报表中当日F消耗量为空值、报表中不存在F）
         *       “用量统计来源”属性：参见化石燃料设置
         *     F的“用量统计来源”为“领料记录”（例如柴油）时：当日F的消耗量为 当日F的生产领料总吨数/总m³数 - 当日F的生产退料总吨数/总m³数
         *       统计当日F的生产领料/生产退料总吨数/总m³数时，查询满足下列条件的出库/入库单
         *         类型为“生产领料”/“生产退料”
         *         状态为“已确认”
         *         确认时间在区间(D 8:00:00, D+1 8:00:00]内
         *         明细中包含F
         *       若查询不到满足条件的出库/入库单，C计为空值
         * 低位发热量、单位热值含碳量、碳氧化率取自当日F对应有效期的设置，若取不到值C计为空值（例如F的设置均已过期）
         * 小数点后最多2位
         */
        //1.从数据库表中获取有效期内的结果
        List<CeFossilFuelSetting> validList = list(new LambdaQueryWrapper<CeFossilFuelSetting>()
                .not(lr -> lr.gt(CeFossilFuelSetting::getValidStart, end)
                        .or()
                        .lt(CeFossilFuelSetting::getValidEnd, start)));
        if (CollUtil.isNotEmpty(validList)) {
            //获取领料的物料集合
            Map<String, String> materialIdMap = Maps.newLinkedHashMap();
            validList.forEach(ele -> {
                materialIdMap.put(ele.getCountSource() + "_" + ele.getMaterialId(), ele.getMaterialName());
            });
            List<Long> materialIdList = validList.stream().filter(ele -> EpManagerConstant.CarbonCountSourceEnum.PICK_MATERIAL.getCode().equals(ele.getCountSource())).map(CeFossilFuelSetting::getMaterialId).distinct().collect(Collectors.toList());
            Map<Long, Map<LocalDate, BigDecimal>> consumeMap = Maps.newHashMap();
            //这里考虑性能-优先考虑异步
            for (Long materialId : materialIdList) {
                Map<LocalDate, BigDecimal> valueMap = FeignOptional.of(() -> remoteWmsStorageReceiptFeignApi.produceConsumeQuantity(new TripleDTO<>(materialId, start, end)), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
                consumeMap.put(materialId, valueMap);
            }
            //如果包括天然气和燃料油
            long flag1 = validList.stream().filter(ele -> EpManagerConstant.CarbonCountSourceEnum.METER_READ.getCode().equals(ele.getCountSource())).filter(ele -> ele.getMaterialName().contains("天然气")).count();
            long flag2 = validList.stream().filter(ele -> EpManagerConstant.CarbonCountSourceEnum.METER_READ.getCode().equals(ele.getCountSource())).filter(ele -> ele.getMaterialName().contains("燃料油")).count();
            Map<LocalDate, BigDecimal> gasMap = null, oilMap = null;
            if (flag1 != 0) {
                gasMap = epDayCountReportService.consumeValue("天然气", "基片", start, end);
            }
            if (flag2 != 0) {
                oilMap = epDayCountReportService.consumeValue("燃料油", "基片", start, end);
            }
            //开始遍历-形成返回集合
            LocalDate startTmp = start;
            Map<String, Map<LocalDate, BigDecimal>> resMap = Maps.newHashMap();
            while (LocalDateUtil.le(startTmp, end)) {
                LocalDate finalValidStart = startTmp;
                List<CeFossilFuelSetting> settingList = validList.stream()
                        .filter(ele -> LocalDateUtil.isRange(finalValidStart, ele.getValidStart(), ele.getValidEnd(), true, true))
                        .collect(Collectors.toList());
                if (CollUtil.isNotEmpty(settingList)) {
                    for (CeFossilFuelSetting setting : settingList) {
                        Map<LocalDate, BigDecimal> valueMap = null;
                        if (EpManagerConstant.CarbonCountSourceEnum.PICK_MATERIAL.getCode().equals(setting.getCountSource())) {
                            valueMap = consumeMap.get(setting.getMaterialId());
                        } else if (setting.getMaterialName().contains("天然气")) {
                            valueMap = gasMap;
                        } else if (setting.getMaterialName().contains("燃料油")) {
                            valueMap = oilMap;
                        } else {
                            valueMap = Maps.newHashMap();
                        }
                        BigDecimal consumeValue = valueMap.get(finalValidStart);
                        BigDecimal value = BigDecimalUtil.multiply(consumeValue, setting.getLowerHeatingValue());
                        value = BigDecimalUtil.multiply(value, setting.getTceValue());
                        value = BigDecimalUtil.multiply(value, setting.getCoRatio());
                        value = BigDecimalUtil.divide(value, BigDecimalUtil.valueOf("1200"));
                        value = BigDecimalUtil.multiply(value, BigDecimalUtil.valueOf("44"));
                        if (setting.getMaterialName().contains("天然气")) {
                            value = BigDecimalUtil.divide(value, BigDecimalUtil.valueOf("10000"));
                        }
                        if (Objects.nonNull(value)) {
                            resMap.computeIfAbsent(setting.getCountSource() + "_" + setting.getMaterialId(), x -> Maps.newHashMap()).put(finalValidStart, value);
                        }
                    }
                }
                startTmp = startTmp.plusDays(1);
            }
            //考虑同一种物料 不同的类型
            materialIdMap.forEach((k, v) -> {
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




