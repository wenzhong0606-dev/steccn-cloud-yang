package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.data.dto.DwConditionDTO;
import com.steccn.data.dto.DwMeterReadDTO;
import com.steccn.data.dto.DwMeterReadDiffDTO;
import com.steccn.data.feign.RemoteDataPlatformFeignApi;
import com.steccn.data.vo.DwEpMeterCollectVO;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpEnergyMoney;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.domain.EpMeterReadDistant;
import com.steccn.ems.domain.EpTaskExecute;
import com.steccn.ems.dto.DwEpEnergyMoneyDTO;
import com.steccn.ems.dto.EpDwEnergyPricePlanDTO;
import com.steccn.ems.dto.EpDwMeterPartitionDTO;
import com.steccn.ems.mapper.EpInstrumentAccountMapper;
import com.steccn.ems.service.*;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.pms.dto.DwProduceOrderDTO;
import com.steccn.pms.feign.RemotePmsFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 数仓控制器
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EpDwServiceImpl implements EpDwService {

    private final EpInstrumentAccountMapper epInstrumentAccountMapper;
    private final RemotePmsFeignApi remotePmsFeignApi;
    private final RemoteDataPlatformFeignApi remoteDataPlatformFeignApi;
    private final EpMeterReadDistantService epMeterReadDistantService;
    private final EpEnergyPricePlanService epEnergyPricePlanService;
    private final EpEnergyMoneyService epEnergyMoneyService;
    private final EpPartitionService epPartitionService;
    private final EpTaskExecuteService epTaskExecuteService;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final EpMeterReadService epMeterReadService;

    @Override
    public Boolean meterRead(LocalDate date) {
        //1.获取所有的远传仪表+挂载的分区节点
        List<EpDwMeterPartitionDTO> meterList = epInstrumentAccountMapper.meterPartition();
        //2.获取所有有仪表的节点
        Set<Long> allPartitionIdSet = meterList.stream().map(EpDwMeterPartitionDTO::getPartitionId).collect(Collectors.toSet());
        //3.获取仅统计每日能耗（类型II）的节点
        Set<Long> otherPartitionNodeSet = epPartitionService.otherPartition(allPartitionIdSet);
        //4.获取当前班次日期所有订单信息
        List<DwProduceOrderDTO> produceOrderList = FeignOptional.of(() -> remotePmsFeignApi.currentProduceOrder(new TupleDTO<>(date, date)), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        //5.查询所有仪表的产线父节点
        Set<Integer> officeIdSet = meterList.stream().map(EpDwMeterPartitionDTO::getOfficeId).collect(Collectors.toSet());
        Map<Integer, Integer> lineMap = FeignOptional.of(() -> remoteOfficeFeign.parentLine(officeIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //防止fegin获取不到当前请求
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        //6.遍历所有的远传仪表
        for (EpDwMeterPartitionDTO meter : meterList) {
            //一开始 应该划分好任务，然后插入数据库中，加上标识
            //子任务执行好插入数据，更新标识
            //避免重复执行
            List<DwConditionDTO> taskList = read(date, meter, otherPartitionNodeSet, produceOrderList, lineMap);
            for (DwConditionDTO task : taskList) {
                CompletableFuture.runAsync(() ->{
                    try {
                        RequestContextHolder.setRequestAttributes(attributes);
                        //执行任务
                        doMeterRead(task);
                    }catch (Exception e){
                        log.error("{}",e);
                    }
                });
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 执行任务
     *
     * @param task 任务
     * @return
     */
    private boolean doMeterRead(DwConditionDTO task) {
        //执行任务，考虑重复执行的问题
        long count = epMeterReadDistantService.count(new LambdaQueryWrapper<EpMeterReadDistant>()
                .eq(EpMeterReadDistant::getMeterId, task.getMeterId())
                .eq(EpMeterReadDistant::getDistantMapCode, task.getCode())
                .eq(EpMeterReadDistant::getReadDate, task.getReadDate())
                .eq(Objects.nonNull(task.getClassId()), EpMeterReadDistant::getClassId, task.getClassId())
                .eq(Objects.nonNull(task.getThick()), EpMeterReadDistant::getThick, task.getThick())
                .eq(EpMeterReadDistant::getStart, task.getStart())
                .eq(EpMeterReadDistant::getEnd, task.getEnd())
                .eq(Objects.nonNull(task.getProduceOrderId()), EpMeterReadDistant::getProduceOrderId, task.getProduceOrderId()));
        if (count > 0) {
            log.info("读数归档 班次日期 -> {} 已跑过", task);
            return Boolean.FALSE;
        }
        //构造请求
        DwMeterReadDiffDTO readDTO = new DwMeterReadDiffDTO();
        readDTO.setCode(task.getCode());
        readDTO.setStart(task.getStart());
        readDTO.setEnd(task.getEnd());
        BigDecimal diffValue = FeignOptional.of(() -> remoteDataPlatformFeignApi.meterReadDiff(readDTO), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get();
        if (Objects.nonNull(diffValue)) {
            EpMeterReadDistant epMeterRead = new EpMeterReadDistant();
            BeanUtils.copyProperties(task, epMeterRead);
            epMeterRead.setDistantMapCode(task.getCode());
            epMeterRead.setReadValue(diffValue);
            epMeterReadDistantService.save(epMeterRead);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 分解小任务
     *
     * @param date                 当前日期
     * @param meter                当前仪表
     * @param otherNodeSet         仅统计每日能耗（类型II）的节点
     * @param orderList            所有生产订单的信息
     * @param lineMap              产线map
     * @return
     */
    private List<DwConditionDTO> read(LocalDate date, EpDwMeterPartitionDTO meter, Set<Long> otherNodeSet, List<DwProduceOrderDTO> orderList, Map<Integer, Integer> lineMap) {
        List<DwConditionDTO> taskList = Lists.newArrayList();
        if (Objects.nonNull(meter)) {
            //仅统计每日能耗（类型II）的节点
            if (otherNodeSet.contains(meter.getPartitionId())) {
                DwConditionDTO dwConditionDTO = new DwConditionDTO();
                dwConditionDTO.setCode(meter.getDistantMapCode());
                dwConditionDTO.setMeterId(meter.getMeterId());
                dwConditionDTO.setReadDate(date);
                //当前日期的早班时间
                dwConditionDTO.setStart(LocalDateUtil.getMorning(date));
                //当前日期T+1的早班时间
                dwConditionDTO.setEnd(LocalDateUtil.getMorning(date.plusDays(1)));
                //添加到任务集合
                taskList.add(dwConditionDTO);
            } else {
                //节点N是产线节点或产线节点的下级节点：统计能耗考虑班次、产品厚度（类型I）
                //根据仪表挂载的节点获取产线，然后根据产线获取生产订单的开始时间和结束时间
                //基于生产订单的开始时间和结束时间查询能耗

                //获取产线
                Integer line = lineMap.get(meter.getOfficeId());
                //根据产线获取生产订单
                List<DwProduceOrderDTO> subOrderList = orderList.stream().filter(v -> v.getProductLineId().equals(line)).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(subOrderList)) {
                    //有生产订单
                    subOrderList.forEach(ele ->{
                        DwConditionDTO dwConditionDTO = getDwConditionDTO(meter, ele);
                        taskList.add(dwConditionDTO);
                    });
                } else {
                    //没有生产订单
                    LocalDateTime morningLdt = LocalDateUtil.getMorning(date);
                    LocalDateTime nightLdt = LocalDateUtil.getNight(date);
                    LocalDateTime lastLdt = LocalDateUtil.getMorning(date.plusDays(1));
                    DwConditionDTO morningDefault = getDefaultDwConditionDTO(meter, morningLdt, nightLdt, date, 0);
                    DwConditionDTO nightDefault = getDefaultDwConditionDTO(meter, nightLdt, lastLdt, date, 1);
                    taskList.add(morningDefault);
                    taskList.add(nightDefault);
                }
            }
        }
        //将开始时间排序
        taskList.sort(Comparator.comparing(DwConditionDTO::getStart));
        return taskList;
    }

    @Override
    public Boolean electricMoney(LocalDate date) {
        //如果发现date日期任务已完成，则不执行
        EpTaskExecute oldTask = epTaskExecuteService.getOne(new LambdaQueryWrapper<EpTaskExecute>().eq(EpTaskExecute::getTaskDate, date)
                .eq(EpTaskExecute::getType, EpManagerConstant.TaskTypeEnum.TYPE_0.getCode())
                .last(MpConstant.STR_LIMIT_1));
        if (Objects.nonNull(oldTask) && EpManagerConstant.TaskFlagEnum.ALL_FINISHED.getCode().equals(oldTask.getTaskFlag())) {
            //当前日期的任务已完成
            log.info("当前日期的任务已完成");
            return Boolean.FALSE;
        }
        List<EpDwMeterPartitionDTO> meterList = epInstrumentAccountMapper.meterPartition();
        //当前有效的计划
        List<EpDwEnergyPricePlanDTO> priceList = epEnergyPricePlanService.currentValidPlan(date);
        Integer sum = 0 ;
        //防止fegin获取不到当前请求
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        for (EpDwMeterPartitionDTO meter : meterList) {
            //分解出task
            List<DwEpEnergyMoneyDTO> taskList = moneyTask(date, meter, priceList);
            for (DwEpEnergyMoneyDTO ele : taskList) {
                sum++;
                CompletableFuture.runAsync(() ->{
                    try {
                        RequestContextHolder.setRequestAttributes(attributes);
                        doMoney(ele);
                    } catch (Exception e) {
                        log.error("计算money异常 {}", e.getLocalizedMessage());
                    }

                });
            }
        }
        //保存任务条数
        //如果没有当前任务，则需要插入，如果有当前任务，补偿更新
        if (Objects.isNull(oldTask)) {
            oldTask = new EpTaskExecute();
        }
        oldTask.setType(EpManagerConstant.TaskTypeEnum.TYPE_0.getCode());
        oldTask.setTaskDate(date);
        oldTask.setTaskParam(sum);
        oldTask.setTaskFlag(EpManagerConstant.TaskFlagEnum.NOT_START.getCode());
        epTaskExecuteService.saveOrUpdate(oldTask);
        return Boolean.TRUE;
    }

    @Override
    public Boolean read(LocalDate date) {
        //1.获取所有的远传仪表+挂载的分区节点
        List<EpDwMeterPartitionDTO> meterList = epInstrumentAccountMapper.meterPartition();
        //2.获取当前日期的任务
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        meterList.forEach(meter -> {
            //防止fegin获取不到当前请求
            CompletableFuture.runAsync(() ->{
                try {
                    RequestContextHolder.setRequestAttributes(attributes);
                    saveReadMeter(date, meter);
                } catch (Exception e) {
                    log.error("插入抄表数据失败 {}", e.getLocalizedMessage());
                }
            });
        });
        return Boolean.TRUE;
    }

    /**
     * 插入抄表数据
     * @param date
     * @param meter
     * @return
     */
    private boolean saveReadMeter(LocalDate date, EpDwMeterPartitionDTO meter) {
        //避免重复插入
        EpMeterRead one = epMeterReadService.getOne(new LambdaQueryWrapper<EpMeterRead>()
                .eq(EpMeterRead::getMeterId, meter.getMeterId())
                .eq(EpMeterRead::getReadDate, date)
                .eq(EpMeterRead::getDistantMapCode, meter.getDistantMapCode())
                .last(MpConstant.STR_LIMIT_1));
        if (Objects.nonNull(one)) {
            log.info("远传仪表{} 抄表数据已插入", one.getDistantMapCode());
            return Boolean.TRUE;
        }
        DwMeterReadDTO end = new DwMeterReadDTO();
        end.setCode(meter.getDistantMapCode());
        end.setRecordTime(LocalDateUtil.getMorning(date));
        DwEpMeterCollectVO endVo = FeignOptional.of(() -> remoteDataPlatformFeignApi.meterRead(end), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get();

        if (Objects.nonNull(endVo)) {
            EpMeterRead ele = new EpMeterRead();
            ele.setAdminValue(endVo.getValue());
            ele.setReadValue(endVo.getValue());
            ele.setAdminFlag(MpConstant.NO);
            ele.setStart(endVo.getRecordTime());
            ele.setEnd(end.getRecordTime());
            ele.setReadDate(date);
            ele.setMeterId(meter.getMeterId());
            ele.setDistantMapCode(meter.getDistantMapCode());
            return epMeterReadService.save(ele);
        }
        return Boolean.FALSE;
    }

    /**
     * 算钱 -实际任务执行者
     *
     * @param ele 元素
     */
    private Boolean doMoney(DwEpEnergyMoneyDTO ele) {
        //执行任务，考虑重复执行的问题
        long count = epEnergyMoneyService.count(new LambdaQueryWrapper<EpEnergyMoney>()
                .eq(EpEnergyMoney::getClassDate, ele.getClassDate())
                .eq(EpEnergyMoney::getPartitionId, ele.getPartitionId())
                .eq(EpEnergyMoney::getType, ele.getType())
                .eq(EpEnergyMoney::getStart, ele.getStart())
                .eq(EpEnergyMoney::getEnd, ele.getEnd()));
        if (count > 0) {
            log.info("读数归档 班次日期 -> {} 已跑过", ele);
            return Boolean.FALSE;
        }

        DwMeterReadDTO readDTO = new DwMeterReadDTO();
        readDTO.setCode(ele.getCode());
        readDTO.setRecordTime(ele.getStart());
        DwEpMeterCollectVO startVo = FeignOptional.of(() -> remoteDataPlatformFeignApi.meterRead(readDTO), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get();
        readDTO.setCode(ele.getCode());
        readDTO.setRecordTime(ele.getEnd());
        DwEpMeterCollectVO endVo = FeignOptional.of(() -> remoteDataPlatformFeignApi.meterRead(readDTO), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get();

        ele.setStartValue(Objects.nonNull(startVo) ? startVo.getValue() : BigDecimal.ZERO);
        ele.setEndValue(Objects.nonNull(endVo) ? endVo.getValue() : ele.getStartValue());
        EpEnergyMoney epEnergyMoney = new EpEnergyMoney();
        BeanUtils.copyProperties(ele, epEnergyMoney);
        return epEnergyMoneyService.save(epEnergyMoney);
    }

    /**
     * 根据仪表和日期 获取小任务集合
     *
     * @param date
     * @param meter
     * @return
     */
    private List<DwEpEnergyMoneyDTO> moneyTask(LocalDate date, EpDwMeterPartitionDTO meter,List<EpDwEnergyPricePlanDTO> priceList) {
        List<DwEpEnergyMoneyDTO> taskList = Lists.newArrayList();
        if (Objects.nonNull(meter) && Objects.nonNull(date)) {
            //查询当前班次日期有效的能源计费方案明细
            if (CollUtil.isNotEmpty(priceList)) {
                for (EpDwEnergyPricePlanDTO price : priceList) {
                    DwEpEnergyMoneyDTO task = new DwEpEnergyMoneyDTO();
                    BeanUtils.copyProperties(price, task);
                    //班次日期调整
                    if (price.getEnd().equals(LocalTime.of(0, 0, 0))) {
                        task.setEnd(LocalDateTime.of(date.plusDays(1), price.getEnd()));
                    } else {
                        task.setEnd(LocalDateTime.of(date, price.getEnd()));
                    }
                    task.setCode(meter.getDistantMapCode());
                    task.setStart(LocalDateTime.of(date, price.getStart()));
                    task.setPartitionId(meter.getPartitionId());
                    task.setClassDate(date);
                    taskList.add(task);
                }
            }
        }
        return taskList;
    }

    /**
     * 获取默认的能耗数据
     *
     * @param meter    仪表信息
     * @param start    开始时间
     * @param end      结束时间
     * @param readDate 班次日期
     * @return 最早报工数据
     */
    private DwConditionDTO getDefaultDwConditionDTO(EpDwMeterPartitionDTO meter, LocalDateTime start, LocalDateTime end, LocalDate readDate, Integer classId) {
        DwConditionDTO dwConditionDTO = new DwConditionDTO();
        dwConditionDTO.setMeterId(meter.getMeterId());
        dwConditionDTO.setCode(meter.getDistantMapCode());
        dwConditionDTO.setReadDate(readDate);
        dwConditionDTO.setStart(start);
        dwConditionDTO.setEnd(end);
        dwConditionDTO.setThick(null);
        dwConditionDTO.setClassId(classId);
        return dwConditionDTO;
    }

    /**
     * 构造元素
     *
     * @param meter 仪表信息
     * @param data  报工数据
     * @return dw数仓查询对象
     */
    private DwConditionDTO getDwConditionDTO(EpDwMeterPartitionDTO meter, DwProduceOrderDTO data) {
        DwConditionDTO dwConditionDTO = new DwConditionDTO();
        dwConditionDTO.setMeterId(meter.getMeterId());
        dwConditionDTO.setCode(meter.getDistantMapCode());
        dwConditionDTO.setReadDate(data.getClassDate());
        dwConditionDTO.setClassId(data.getClassId());
        dwConditionDTO.setThick(data.getThick());
        dwConditionDTO.setStart(data.getStart());
        dwConditionDTO.setEnd(data.getEnd());
        dwConditionDTO.setProduceOrderId(data.getProduceOrderId());
        return dwConditionDTO;
    }
}
