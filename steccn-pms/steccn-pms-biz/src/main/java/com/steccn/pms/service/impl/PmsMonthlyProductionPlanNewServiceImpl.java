package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
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
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.MapUtil;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmsMonthlyProductionPlanNew;
import com.steccn.pms.mapper.PmsMonthlyProductionPlanNewMapper;
import com.steccn.pms.service.PmsMonthlyProductionPlanNewService;
import com.steccn.pms.service.PmsMonthlyProductionPlanService;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.pms.vo.PmsMonthlyProductionPlanNewListVO;
import com.steccn.pms.vo.PmsMonthlyProductionPlanNewRowVO;
import com.steccn.wms.dto.WmsDeliveryReceiptClientNumDTO;
import com.steccn.wms.feign.RemoteWmsDeliveryReceiptFeignApi;
import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
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
    private final PmsReportWorkService pmsReportWorkService;
    private final RemoteWmsDeliveryReceiptFeignApi remoteWmsDeliveryReceiptFeignApi;
    @Resource
    private PmsMonthlyProductionPlanService pmsMonthlyProductionPlanService;

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
        if (Objects.isNull(linkId)) return Boolean.FALSE;
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
        //可能是新增数据、可能是更新数据
        List<PmsMonthlyProductionPlanNew> dbList = list(new LambdaQueryWrapper<PmsMonthlyProductionPlanNew>()
                .eq(PmsMonthlyProductionPlanNew::getMaterialId, materialId)
                .eq(PmsMonthlyProductionPlanNew::getClientId, clientId)
                .ge(PmsMonthlyProductionPlanNew::getPlanDate, monthFirstDay)
                .le(PmsMonthlyProductionPlanNew::getPlanDate, monthLastDay));
        Assert.notEmpty(dbList, "本月度暂无此物料与客户数据");
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
        Long linkId = dbList.get(0).getLinkId();
        valueMap.forEach((k, v) -> {
            PmsMonthlyProductionPlanNew model = new PmsMonthlyProductionPlanNew();
            model.setMaterialId(materialId);
            model.setClientId(clientId);
            model.setPlanDate(k);
            model.setClient(client);
            model.setPlanQuantity(v);
            model.setState(0);
            model.setLinkId(linkId);
            dbList.add(model);
        });
        boolean flag = saveOrUpdateBatch(dbList);
        //需要更新计划数量
        if (Objects.nonNull(linkId)) {
            pmsMonthlyProductionPlanService.editPlanQuantity(linkId, BigDecimalUtil.sum(dbList.stream().map(PmsMonthlyProductionPlanNew::getPlanQuantity).collect(Collectors.toList())));
        }
        return flag;
    }

    @Override
    public PmsMonthlyProductionPlanNewListVO show(LocalDate monthFirstDay) {
        LocalDate monthLastDay = LocalDateUtil.monthLastDay(monthFirstDay);
        PmsMonthlyProductionPlanNewListVO resVo = new PmsMonthlyProductionPlanNewListVO();
        List<PmsMonthlyProductionPlanNewRowVO> dataList = Lists.newArrayList();
        List<String> dateStrList = Lists.newArrayList();
        LocalDate end = monthLastDay;
        //构建动态表头
        List<LocalDate> dateList = LocalDateUtil.getLocalDateByStartEndDate(monthFirstDay, end);
        dateList.forEach(ele -> dateStrList.add(LocalDateTimeUtil.format(ele, "MM-dd")));
        resVo.setDateList(dateStrList);

        //获取当前月的最后一天
        List<PmsMonthlyProductionPlanNew> dbList = list(new LambdaQueryWrapper<PmsMonthlyProductionPlanNew>()
                .ge(PmsMonthlyProductionPlanNew::getPlanDate, monthFirstDay)
                .le(PmsMonthlyProductionPlanNew::getPlanDate, monthLastDay)
                .orderByDesc(PmsMonthlyProductionPlanNew::getCreateTime));
        Map<String, BigDecimal> datePlanValueMap = Maps.newHashMap();
        Map<String, BigDecimal> dateSumPlanValueMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(dbList)) {
            List<Long> materialIdList = dbList.stream().map(PmsMonthlyProductionPlanNew::getMaterialId).distinct().collect(Collectors.toList());
            List<Integer> clientIdList = dbList.stream().map(PmsMonthlyProductionPlanNew::getClientId).distinct().collect(Collectors.toList());
            Set<Integer> clientIdSet = Sets.newHashSet(clientIdList);
            Map<Long, EqpSpareVO> materialIdMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIdList), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Integer, String> clientMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(clientIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Set<String> clientNameSet = clientMap.values().stream().collect(Collectors.toSet());
            Map<String, Long> materialClientMap = Maps.newHashMap();
            dbList.forEach(ele -> {
                materialClientMap.put(ele.getMaterialId() + ":" + ele.getClientId(), ele.getLinkId());
                datePlanValueMap.put(ele.getMaterialId() + ":" + ele.getClientId() + ":" + ele.getPlanDate(), ele.getPlanQuantity());
                MapUtil.add(dateSumPlanValueMap, ele.getMaterialId() + ":" + ele.getPlanDate(), ele.getPlanQuantity(), BigDecimalUtil::add);
            });
            //获取发货数量
            Map<String, BigDecimal> clientValueMap = Maps.newHashMap();
            Map<String, BigDecimal> clientValueSumMap = Maps.newHashMap();
            List<WmsDeliveryReceiptClientNumDTO> clientNumList = FeignOptional.of(() -> remoteWmsDeliveryReceiptFeignApi.clientNum(new TripleDTO<>(Sets.newHashSet(materialIdList), monthFirstDay, monthLastDay)), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
            clientNumList = clientNumList.stream().filter(v -> LocalDateUtil.isRange(v.getClassDate(), monthFirstDay, monthLastDay, true, true) && clientNameSet.contains(v.getClientName())).collect(Collectors.toList());
            clientNumList.forEach(ele -> {
                MapUtil.add(clientValueMap, ele.getMaterialId() + ":" + ele.getClientName() + ":" + ele.getClassDate(), ele.getNum(), BigDecimalUtil::add);
                MapUtil.add(clientValueSumMap, ele.getMaterialId() + ":" + ele.getClassDate(), ele.getNum(), BigDecimalUtil::add);
            });
            //获取物料的入库总数
            Map<String, BigDecimal> inStorageMaterialMap = pmsReportWorkService.dateMaterialInStorageUnit(materialIdList, monthFirstDay, monthLastDay, PmManagerConstant.MEASURE_UNIT_PIECE);
            for (Long materialId : materialIdList) {
                //获取物料数据
                EqpSpareVO eqpSpareVO = materialIdMap.getOrDefault(materialId, new EqpSpareVO());
                for (Integer clientId : clientIdList) {
                    String clientName = clientMap.get(clientId);
                    Long linkId = materialClientMap.get(materialId + ":" + clientId);
                    if (Objects.nonNull(linkId) || materialClientMap.containsKey(materialId + ":" + clientId)) {
                        PmsMonthlyProductionPlanNewRowVO planRowVo = getSummary(1, new TripleDTO<>(clientId, clientName, "生产计划")
                                , eqpSpareVO, dateList, x -> datePlanValueMap.get(materialId + ":" + clientId + ":" + x));
                        planRowVo.setState(Objects.nonNull(linkId) ? 1 : 0);
                        planRowVo.setLinkId(linkId);
                        PmsMonthlyProductionPlanNewRowVO outRowVo = getSummary(2, new TripleDTO<>(clientId, clientName, "发货数量")
                                , eqpSpareVO, dateList, x -> clientValueMap.get(materialId + ":" + clientName + ":" + x));
                        outRowVo.setState(planRowVo.getState());
                        outRowVo.setLinkId(linkId);
                        dataList.add(planRowVo);
                        dataList.add(outRowVo);
                    }
                }
                //获取物料的期初库存
                //获取物料的计划总数
                dataList.add(getSummary(3, new TripleDTO<>(-1, "计划总数", "计划总数"), eqpSpareVO, dateList, x -> dateSumPlanValueMap.get(materialId + ":" + x)));
                //获取物料的发货总数
                dataList.add(getSummary(4, new TripleDTO<>(-1, "发货总数", "发货总数"), eqpSpareVO, dateList, x -> clientValueSumMap.get(materialId + ":" + x)));
                //获取物料的入库总数
                dataList.add(getSummary(6, new TripleDTO<>(-1, "入库总数", "入库总数"), eqpSpareVO, dateList, x -> inStorageMaterialMap.get(x + "_" + materialId)));
            }
            resVo.setDataList(dataList);
        }
        return resVo;
    }

    /**
     * 获取汇总合计
     *
     * @param type       类型 生产计划1 发货数量2 计划总数3 发货总数4 期初库存5 入库总数6
     * @param param      指标名称
     * @param eqpSpareVO 物料信息
     * @param dateList   日期集合
     * @param function   取值方法表达式
     * @return
     */
    private PmsMonthlyProductionPlanNewRowVO getSummary(Integer type, TripleDTO<Integer, String, String> param,
                                                        EqpSpareVO eqpSpareVO,
                                                        List<LocalDate> dateList,
                                                        Function<LocalDate, BigDecimal> function) {
        PmsMonthlyProductionPlanNewRowVO rowVo = new PmsMonthlyProductionPlanNewRowVO();
        rowVo.setType(type);
        rowVo.setMaterialId(eqpSpareVO.getId());
        rowVo.setMaterialCode(eqpSpareVO.getCode());
        rowVo.setMaterialName(eqpSpareVO.getName());
        rowVo.setClientId(param.getField1());
        rowVo.setClient(param.getField2());
        rowVo.setIndicatorName(param.getField3());
        List<BigDecimal> valueList = Lists.newArrayList();
        //遍历日期
        for (LocalDate date : dateList) {
            valueList.add(BigDecimalUtil.setScale(function.apply(date), 0));
        }
        rowVo.setValue(valueList);
        rowVo.setSum(BigDecimalUtil.setScale(BigDecimalUtil.sum(valueList), 0));
        return rowVo;
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




