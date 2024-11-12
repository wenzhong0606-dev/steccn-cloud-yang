package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.MapUtil;
import com.steccn.ems.vo.EpDayReportCountHeadVO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.domain.PmsMonthlyProductionPlanNew;
import com.steccn.pms.mapper.PmsMonthlyProductionPlanNewMapper;
import com.steccn.pms.service.PmsMonthlyProductionPlanNewService;
import com.steccn.pms.vo.PmsMonthlyProductionPlanNewListVO;
import com.steccn.pms.vo.PmsMonthlyProductionPlanNewRowVO;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.jni.Local;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_monthly_production_plan_new】的数据库操作Service实现
 * @createDate 2024-11-11 13:54:35
 */
@Service
@RequiredArgsConstructor
public class PmsMonthlyProductionPlanNewServiceImpl extends ServiceImpl<PmsMonthlyProductionPlanNewMapper, PmsMonthlyProductionPlanNew>
        implements PmsMonthlyProductionPlanNewService {

    private final RemoteOfficeFeign remoteOfficeFeign;
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final Redisson redisson;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertData(LocalDate monthFirstDay, Integer clientId, Long materialId) {
        //获取客户id
        SysOfficeVO clientVo = FeignOptional.of(() -> remoteOfficeFeign.info(clientId), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(clientVo, "客户不存在");
        EqpSpareVO materialVo = FeignOptional.of(() -> remoteSpareFeignApi.getById(materialId), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(materialVo, "物料不存在");
        //加分布式锁-反正出现有效期重叠的情况
        RLock lock = redisson.getLock(materialId + ":" + clientId + ":" + monthFirstDay);
        lock.lock();
        try {
            //获取当前月的最后一天
            LocalDate monthLastDay = LocalDateUtil.monthLastDay(monthFirstDay);
            //判断当前月物料客户已存在，不可重复添加
            long count = count(new LambdaQueryWrapper<PmsMonthlyProductionPlanNew>()
                    .eq(PmsMonthlyProductionPlanNew::getMaterialId, materialId)
                    .eq(PmsMonthlyProductionPlanNew::getClientId, clientId)
                    .ge(PmsMonthlyProductionPlanNew::getPlanDate, monthFirstDay)
                    .le(PmsMonthlyProductionPlanNew::getPlanDate, monthLastDay));
            Assert.isTrue(count == 0, "当前月物料客户已存在，不可重复添加");
            PmsMonthlyProductionPlanNew model = new PmsMonthlyProductionPlanNew();
            model.setMaterialId(materialId);
            model.setClientId(clientId);
            model.setPlanDate(monthFirstDay);
            model.setClient(clientVo.getOfficeName());
            model.setPlanQuantity(BigDecimal.ZERO);
            model.setState(0);
            return save(model);
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteData(LocalDate monthFirstDay, Integer clientId, Long materialId) {
        //如果已下达，就不可以删除
        //加分布式锁-反正出现有效期重叠的情况
        //获取当前月的最后一天
        LocalDate monthLastDay = LocalDateUtil.monthLastDay(monthFirstDay);
        RLock lock = redisson.getLock(materialId + ":" + clientId + ":" + monthFirstDay);
        lock.lock();
        try {
            //判断当前月物料客户已存在，不可重复添加
            judgeRelease(clientId, materialId, monthFirstDay, monthLastDay, "已下达状态下不可以删除");
            boolean remove = remove(new LambdaQueryWrapper<PmsMonthlyProductionPlanNew>()
                    .eq(PmsMonthlyProductionPlanNew::getMaterialId, materialId)
                    .eq(PmsMonthlyProductionPlanNew::getClientId, clientId)
                    .ge(PmsMonthlyProductionPlanNew::getPlanDate, monthFirstDay)
                    .le(PmsMonthlyProductionPlanNew::getPlanDate, monthLastDay));
            return remove;
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean release(Long linkId, String date, Integer clientId, Long materialId) {
        Assert.notNull(linkId, "关联月度计划主键为空");
        LocalDate monthFirstDay;
        try {
            date = date + "-01";
            monthFirstDay = LocalDate.parse(date);
        } catch (Exception e) {
            throw new BusinessException("日期格式异常");
        }
        //获取当前月的最后一天
        LocalDate monthLastDay = LocalDateUtil.monthLastDay(monthFirstDay);
        //已下达状态不可以继续下达
        judgeRelease(clientId, materialId, monthFirstDay, monthLastDay, "已下达状态下不可以再下达");
        //月份
        boolean update = update(new LambdaUpdateWrapper<PmsMonthlyProductionPlanNew>()
                .set(PmsMonthlyProductionPlanNew::getState, MpConstant.YES)
                .set(PmsMonthlyProductionPlanNew::getLinkId, linkId)
                .eq(PmsMonthlyProductionPlanNew::getMaterialId, materialId)
                .eq(PmsMonthlyProductionPlanNew::getClientId, clientId)
                .ge(PmsMonthlyProductionPlanNew::getPlanDate, monthFirstDay)
                .le(PmsMonthlyProductionPlanNew::getPlanDate, monthLastDay));
        return update;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateData(LocalDate monthFirstDay, Integer clientId, Long materialId, List<TupleDTO<LocalDate, BigDecimal>> value) {
        Assert.notNull(value, "更新数据集为空");
        //获取当前月的最后一天
        LocalDate monthLastDay = LocalDateUtil.monthLastDay(monthFirstDay);
        judgeRelease(clientId, materialId, monthFirstDay, monthLastDay, "已下达状态下不可以编辑");
        //可能是新增数据、可能是更新数据
        List<PmsMonthlyProductionPlanNew> dbList = list(new LambdaQueryWrapper<PmsMonthlyProductionPlanNew>()
                .eq(PmsMonthlyProductionPlanNew::getMaterialId, materialId)
                .eq(PmsMonthlyProductionPlanNew::getClientId, clientId)
                .ge(PmsMonthlyProductionPlanNew::getPlanDate, monthFirstDay)
                .le(PmsMonthlyProductionPlanNew::getPlanDate, monthLastDay));
        Assert.notNull(dbList, "本月度暂无此物料与客户数据");
        //将日期从list转换成map
        Map<LocalDate, BigDecimal> valueMap = value.stream().filter(ele -> LocalDateUtil.isRange(ele.getField1(), monthFirstDay, monthLastDay, true, true)).collect(Collectors.toMap(TupleDTO::getField1, TupleDTO::getField2));
        //编辑
        dbList.forEach(ele -> {
            BigDecimal bigDecimal = valueMap.get(ele.getPlanDate());
            ele.setPlanQuantity(bigDecimal);
            valueMap.remove(ele.getPlanDate());
        });
        //新增
        String client = dbList.get(0).getClient();
        valueMap.forEach((k, v) -> {
            PmsMonthlyProductionPlanNew model = new PmsMonthlyProductionPlanNew();
            model.setMaterialId(materialId);
            model.setClientId(clientId);
            model.setPlanDate(k);
            model.setClient(client);
            model.setPlanQuantity(v);
            model.setState(0);
            dbList.add(model);
        });
        return saveOrUpdateBatch(dbList);
    }

    @Override
    public PmsMonthlyProductionPlanNewListVO show(LocalDate monthFirstDay) {
        LocalDate monthLastDay = LocalDateUtil.monthLastDay(monthFirstDay);
        PmsMonthlyProductionPlanNewListVO resVo = new PmsMonthlyProductionPlanNewListVO();
        List<PmsMonthlyProductionPlanNewRowVO> dataList = Lists.newArrayList();
        List<String> dateStrList = Lists.newArrayList();
        LocalDate classDate = LocalDateUtil.getClassDate(LocalDateTime.now(), false);
        LocalDate end = LocalDateUtil.ge(classDate, monthLastDay) ? monthLastDay : classDate;
        //构建动态表头
        List<LocalDate> dateList = LocalDateUtil.getLocalDateByStartEndDate(monthFirstDay, end);
        dateList.forEach(ele -> dateStrList.add(LocalDateTimeUtil.format(ele, "MM-dd")));
        resVo.setDateList(dateStrList);

        //获取当前月的最后一天
        List<PmsMonthlyProductionPlanNew> dbList = list(new LambdaQueryWrapper<PmsMonthlyProductionPlanNew>()
                .ge(PmsMonthlyProductionPlanNew::getPlanDate, monthFirstDay)
                .le(PmsMonthlyProductionPlanNew::getPlanDate, monthLastDay)
                .orderByDesc(PmsMonthlyProductionPlanNew::getCreateTime));
        Map<String, BigDecimal> dateValueMap = Maps.newLinkedHashMap();
        if (CollUtil.isNotEmpty(dbList)) {
            List<Long> materialIdList = dbList.stream().map(PmsMonthlyProductionPlanNew::getMaterialId).distinct().collect(Collectors.toList());
            List<Integer> clientIdList = dbList.stream().map(PmsMonthlyProductionPlanNew::getClientId).distinct().collect(Collectors.toList());
            Map<Long, EqpSpareVO> materialIdMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIdList), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Integer, SysOfficeVO> clientMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(clientIdList)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Set<String> materialClientSet = Sets.newHashSet();
            dbList.forEach(ele -> {
                materialClientSet.add(ele.getMaterialId() + ":" + ele.getClientId());
                dateValueMap.put(ele.getMaterialId() + ":" + ele.getClientId() + ":" + ele.getPlanDate(), ele.getPlanQuantity());
            });

            for (Long materialId : materialIdList) {
                //获取物料数据
                EqpSpareVO eqpSpareVO = materialIdMap.getOrDefault(materialId, new EqpSpareVO());
                Map<LocalDate, BigDecimal> materialValueMap = Maps.newHashMap();
                for (Integer clientId : clientIdList) {
                    SysOfficeVO clientEntry = clientMap.getOrDefault(clientId, new SysOfficeVO());
                    if (materialClientSet.contains(materialId + ":" + clientId)) {
                        //过滤数据
                        List<BigDecimal> dateValueList = Lists.newArrayList();
                        //遍历日期
                        for (LocalDate date : dateList) {
                            BigDecimal value = dateValueMap.get(materialId + ":" + clientId + ":" + date);
                            dateValueList.add(BigDecimalUtil.setScale(value, 0));
                            MapUtil.add(materialValueMap, date, value, BigDecimalUtil::add);
                        }
                        PmsMonthlyProductionPlanNewRowVO rowVO = new PmsMonthlyProductionPlanNewRowVO();
                        rowVO.setMaterialId(eqpSpareVO.getId());
                        rowVO.setMaterialCode(eqpSpareVO.getCode());
                        rowVO.setMaterialName(eqpSpareVO.getName());
                        rowVO.setClientId(clientId);
                        rowVO.setClient(clientEntry.getOfficeName());
                        rowVO.setIndicatorName("生产计划");
                        rowVO.setValue(dateValueList);
                        rowVO.setSum(BigDecimalUtil.setScale(BigDecimalUtil.sum(dateValueList), 0));
                        dataList.add(rowVO);
                    }
                }
                //获取物料的计划总数
                //获取物料的发货总数
                //获取物料的期初库存
                //获取物料的入库总数
                PmsMonthlyProductionPlanNewRowVO rowVO = new PmsMonthlyProductionPlanNewRowVO();
                rowVO.setMaterialId(eqpSpareVO.getId());
                rowVO.setMaterialCode(eqpSpareVO.getCode());
                rowVO.setMaterialName(eqpSpareVO.getName());
                rowVO.setClientId(-1);
                rowVO.setClient("计划总数");
                rowVO.setIndicatorName("计划总数");
                List<BigDecimal> dateValueSumList = Lists.newArrayList();
                //遍历日期
                for (LocalDate date : dateList) {
                    dateValueSumList.add(BigDecimalUtil.setScale(materialValueMap.get(date), 0));
                }
                rowVO.setValue(dateValueSumList);
                rowVO.setSum(BigDecimalUtil.setScale(BigDecimalUtil.sum(dateValueSumList), 0));
                dataList.add(rowVO);
            }
            resVo.setDataList(dataList);
        }
        return resVo;
    }

    /**
     * 判断是否已下达
     *
     * @param clientId      客户主键
     * @param materialId    物料书剑
     * @param monthFirstDay 月头
     * @param monthLastDay  月末
     * @param hintMsg       提示消息
     */
    private void judgeRelease(Integer clientId, Long materialId, LocalDate monthFirstDay, LocalDate monthLastDay, String hintMsg) {
        long count = count(new LambdaQueryWrapper<PmsMonthlyProductionPlanNew>()
                .eq(PmsMonthlyProductionPlanNew::getMaterialId, materialId)
                .eq(PmsMonthlyProductionPlanNew::getClientId, clientId)
                .ge(PmsMonthlyProductionPlanNew::getPlanDate, monthFirstDay)
                .le(PmsMonthlyProductionPlanNew::getPlanDate, monthLastDay)
                .eq(PmsMonthlyProductionPlanNew::getState, MpConstant.YES));
        Assert.isTrue(count == 0, hintMsg);
    }

}




