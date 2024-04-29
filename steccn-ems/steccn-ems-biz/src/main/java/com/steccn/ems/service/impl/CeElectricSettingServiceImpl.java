package com.steccn.ems.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.GetValueUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.CeElectricSetting;
import com.steccn.ems.dto.CeElectricSettingDTO;
import com.steccn.ems.mapper.CeElectricSettingMapper;
import com.steccn.ems.service.CeElectricSettingService;
import com.steccn.ems.service.EpDayCountReportService;
import com.steccn.ems.vo.CeElectricSettingVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
    private final EpDayCountReportService epDayCountReportService;

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

    @Override
    public Map<LocalDate, BigDecimal> carbonSumReportDay(LocalDate start, LocalDate end) {
        Map<LocalDate, BigDecimal> resMap = Maps.newHashMap();
        /**
         * 电力：日期D的碳排放量C = 当日净购入电量 * 排放因子
         *         当日净购入电力
         * “用量统计来源”为“抄表记录”时：当日净购入电力为 当日全厂总电耗量 - 当日光伏发电量 - 当日余热发电量
         * “用量统计来源”属性：参见电力设置
         *         当日全厂总电耗量取自【能耗日统计报表】中“电”的当日合计值（转换为MWh）
         *         当日光伏发电量取自【能耗日统计报表】中“光伏发电一”、“光伏发电二”的当日数值加总（转换为MWh）
         *         当日余热发电量取自【能耗日统计报表】中“余热发电”的当日数值（转换为MWh）
         * “用量统计来源”为“领料记录”时：暂无此情况
         * 排放因子取自当日对应有效期的设置，若取不到值C计为空值（例如所有设置均已过期）
         * 小数点后最多2位
         */
        //1.从数据库表中获取有效期内的结果
        List<CeElectricSetting> validList = list(new LambdaQueryWrapper<CeElectricSetting>()
                .not(lr -> lr.gt(CeElectricSetting::getValidStart, end)
                        .or()
                        .lt(CeElectricSetting::getValidEnd, start)));
        //2.查询
        if (CollUtil.isNotEmpty(validList)) {
            //当日全厂总电耗量取自【能耗日统计报表】中“电”的当日合计值（转换为MWh）
            Map<LocalDate, BigDecimal> map1 = epDayCountReportService.consumeValue("电", "合计", start, end);
            // 当日光伏发电量取自【能耗日统计报表】中“光伏发电一”、“光伏发电二”的当日数值加总（转换为MWh）
            Map<LocalDate, BigDecimal> map2 = epDayCountReportService.consumeValue("电（其他）", "光伏发电一", start, end);
            Map<LocalDate, BigDecimal> map3 = epDayCountReportService.consumeValue("电（其他）", "光伏发电二", start, end);
            //当日余热发电量取自【能耗日统计报表】中“余热发电”的当日数值（转换为MWh）
            Map<LocalDate, BigDecimal> map4 = epDayCountReportService.consumeValue("电（其他）", "余热发电", start, end);
            //求当日净购入电力为 当日全厂总电耗量 - 当日光伏发电量 - 当日余热发电量
            LocalDate startTmp = start;
            while (LocalDateUtil.le(startTmp, end)) {
                LocalDate finalValidStart = startTmp;
                Optional<CeElectricSetting> settingOp = validList.stream().filter(ele -> LocalDateUtil.isRange(finalValidStart, ele.getValidStart(), ele.getValidEnd(), true, true)).findFirst();
                if (settingOp.isPresent() && EpManagerConstant.CarbonCountSourceEnum.METER_READ.getCode().equals(settingOp.get().getCountSource())) {
                    CeElectricSetting setting = settingOp.get();
                    BigDecimal map1Value = GetValueUtil.get(map1, x -> x.get(finalValidStart), BigDecimal.ZERO);
                    BigDecimal map2Value = GetValueUtil.get(map2, x -> x.get(finalValidStart), BigDecimal.ZERO);
                    BigDecimal map3Value = GetValueUtil.get(map3, x -> x.get(finalValidStart), BigDecimal.ZERO);
                    BigDecimal map4Value = GetValueUtil.get(map4, x -> x.get(finalValidStart), BigDecimal.ZERO);
                    BigDecimal value = BigDecimalUtil.subtract(map1Value, map2Value);
                    value = BigDecimalUtil.subtract(value, map3Value);
                    value = BigDecimalUtil.subtract(value, map4Value);
                    value = BigDecimalUtil.divide(value, BigDecimalUtil.THOUSAND);
                    resMap.put(startTmp, BigDecimalUtil.multiply(value, setting.getFactorValue()));
                }
                startTmp = startTmp.plusDays(1);
            }
        }
        return resMap;
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