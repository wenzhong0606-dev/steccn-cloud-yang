package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.CalculateDateUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.data.dto.DwConditionDTO;
import com.steccn.data.dto.DwMeterReadDTO;
import com.steccn.data.feign.RemoteDataPlatformFeignApi;
import com.steccn.data.vo.DwEpMeterCollectVO;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpEnergyMoney;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.domain.EpTaskExecute;
import com.steccn.ems.dto.DwEpEnergyMoneyDTO;
import com.steccn.ems.dto.EpDwEnergyPricePlanDTO;
import com.steccn.ems.dto.EpDwMeterPartitionDTO;
import com.steccn.ems.mapper.EpInstrumentAccountMapper;
import com.steccn.ems.service.*;
import com.steccn.pms.dto.DwCurrentReportWorkDTO;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.qms.constant.QtManagerConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

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
    private final EpMeterReadService epMeterReadService;
    private final EpEnergyPricePlanService epEnergyPricePlanService;
    private final EpEnergyMoneyService epEnergyMoneyService;
    private final EpPartitionService epPartitionService;
    private final EpTaskExecuteService epTaskExecuteService;
    private final TransactionTemplate transactionTemplate;

    private final ThreadPoolExecutor moneyPool = new ThreadPoolExecutor(1, 5, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), Executors.defaultThreadFactory());

    private final ThreadPoolExecutor readPool = new ThreadPoolExecutor(1, 5, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), Executors.defaultThreadFactory());


    @Override
    public Boolean meterRead(LocalDate date) {
        //1.获取所有的远传仪表+挂载的分区节点
        List<EpDwMeterPartitionDTO> meterList = epInstrumentAccountMapper.meterPartition();
        //2.获取所有有仪表的节点
        Set<Long> allPartitionIdSet = meterList.stream().map(EpDwMeterPartitionDTO::getPartitionId).collect(Collectors.toSet());
        //3.获取仅统计每日能耗（类型II）的节点
        Set<Long> otherPartitionNodeSet = epPartitionService.otherPartition(allPartitionIdSet);
        //4.获取当前班次日期所有的报工信息
        List<DwCurrentReportWorkDTO> allReportWorkList = FeignOptional.of(() -> remotePmsFeignApi.currentReportWork(new TupleDTO<>(date, date)), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        //5.遍历所有的远传仪表
        for (EpDwMeterPartitionDTO meter : meterList) {
            //一开始 应该划分好任务，然后插入数据库中，加上标识
            //子任务执行好插入数据，更新标识
            //避免重复执行
            List<DwConditionDTO> taskList = read(date, meter, otherPartitionNodeSet, allReportWorkList);
            for (DwConditionDTO task : taskList) {
                readPool.submit(() -> {
                    doMeterRead(task);
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
        long count = epMeterReadService.count(new LambdaQueryWrapper<EpMeterRead>()
                .eq(EpMeterRead::getMeterId, task.getMeterId())
                .eq(EpMeterRead::getDistantMapCode, task.getCode())
                .eq(EpMeterRead::getReadDate, task.getReadDate())
                .eq(Objects.nonNull(task.getClassId()), EpMeterRead::getClassId, task.getClassId())
                .eq(Objects.nonNull(task.getThick()), EpMeterRead::getThick, task.getThick())
                .eq(Objects.nonNull(task.getReportWorkId()), EpMeterRead::getReportWorkId, task.getReportWorkId())
                .eq(EpMeterRead::getStart, task.getStart())
                .eq(EpMeterRead::getEnd, task.getEnd()));
        if (count > 0) {
            log.info("读数归档 班次日期 -> {} 已跑过", task);
            return Boolean.FALSE;
        }
        //构造请求
        DwMeterReadDTO readDTO = new DwMeterReadDTO();
        readDTO.setCode(task.getCode());
        readDTO.setRecordTime(task.getEnd());
        DwEpMeterCollectVO endVO = FeignOptional.of(() -> remoteDataPlatformFeignApi.meterRead(readDTO), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get();
        if (Objects.nonNull(endVO)) {
            EpMeterRead epMeterRead = new EpMeterRead();
            BeanUtils.copyProperties(task, epMeterRead);
            epMeterRead.setDistantMapCode(task.getCode());
            epMeterRead.setAdminValue(endVO.getValue());
            epMeterRead.setCorrectValue(endVO.getValue());
            epMeterRead.setReadValue(endVO.getValue());
            epMeterRead.setAdminFlag(0);
            epMeterReadService.save(epMeterRead);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }


    /**
     * 计算表读数
     *
     * @param date                  日期
     * @param meter                 仪表
     * @param otherPartitionNodeSet
     * @param allReportWorkList     所有报工信息
     * @return
     */
    private List<DwConditionDTO> read(LocalDate date, EpDwMeterPartitionDTO meter, Set<Long> otherPartitionNodeSet, List<DwCurrentReportWorkDTO> allReportWorkList) {
        List<DwConditionDTO> taskList = Lists.newArrayList();
        if (Objects.nonNull(meter)) {
            //仅统计每日能耗（类型II）的节点
            if (otherPartitionNodeSet.contains(meter.getPartitionId())) {
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
                //根据仪表挂载的节点获取报工记录
                //获取产线的报工集合
                List<DwCurrentReportWorkDTO> lineList = allReportWorkList.stream().filter(v -> meter.getOfficeId().equals(v.getProductLineId())).collect(Collectors.toList());
                //获取工位的报工集合
                List<DwCurrentReportWorkDTO> stationList = allReportWorkList.stream().filter(v -> meter.getOfficeId().equals(v.getStationId())).collect(Collectors.toList());
                //如果2个都为空
                if (CollUtil.isEmpty(lineList) && CollUtil.isEmpty(stationList)) {
                    LocalDateTime morningLdt = LocalDateUtil.getMorning(date);
                    LocalDateTime nightLdt = LocalDateUtil.getNight(date);
                    LocalDateTime lastLdt = LocalDateUtil.getMorning(date.plusDays(1));
                    //查询2条数据 T日8：00 T日20：00 T+1日8：00
                    DwConditionDTO morningDefault = getDefaultDwConditionDTO(meter, morningLdt, nightLdt, date, 0);
                    DwConditionDTO nightDefault = getDefaultDwConditionDTO(meter, nightLdt, lastLdt, date, 1);

                    //任务集合
                    taskList.add(morningDefault);
                    taskList.add(nightDefault);
                } else {
                    //产线的能耗集合
                    List<DwConditionDTO> lineTaskList = dwConditionDTO(date, meter, lineList);
                    List<DwConditionDTO> stationTaskList = dwConditionDTO(date, meter, stationList);
                    taskList.addAll(lineTaskList);
                    taskList.addAll(stationTaskList);
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
        if (Objects.nonNull(oldTask) && EpManagerConstant.TaskFlagEnum.NOT_START.getCode().equals(oldTask.getTaskFlag())) {
            //当前日期的任务已完成
            log.info("当前日期的任务已完成");
            return Boolean.FALSE;
        }
        List<EpDwMeterPartitionDTO> meterList = epInstrumentAccountMapper.meterPartition();
        //当前有效的计划
        List<EpDwEnergyPricePlanDTO> priceList = epEnergyPricePlanService.currentValidPlan(date);
        Integer sum = 0 ;
        for (EpDwMeterPartitionDTO meter : meterList) {
            //分解出task
            List<DwEpEnergyMoneyDTO> taskList = moneyTask(date, meter, priceList);
            for (DwEpEnergyMoneyDTO ele : taskList) {
                sum ++;
                moneyPool.submit(() ->{
                    doMoney(ele);
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
    private List<DwEpEnergyMoneyDTO> moneyTask(LocalDate date, EpDwMeterPartitionDTO meter, List<EpDwEnergyPricePlanDTO> priceList) {
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
     * 根据报工数据组织一天的数据
     *
     * @param date
     * @param meter
     * @param reportWorkDTO
     */
    private List<DwConditionDTO> dwConditionDTO(LocalDate date, EpDwMeterPartitionDTO meter, List<DwCurrentReportWorkDTO> reportWorkDTO) {
        //定义返回集合
        List<DwConditionDTO> resList = Lists.newArrayList();
        //如果报工为空，则返回空集合[]
        if (CollUtil.isEmpty(reportWorkDTO)) {
            return resList;
        }
        //获取一个仪表节点一天的报工数据-需要考虑跨班次的问题
        for (int i = 0; i < reportWorkDTO.size(); i++) {
            DwCurrentReportWorkDTO reportWork = reportWorkDTO.get(i);
            //根据报工 构造数据
            DwConditionDTO reportDwDto = getDwConditionDTO(meter, reportWork);
            //添加报工的时间
            resList.add(reportDwDto);
        }
        //获取每个报工之间的空闲时间
        resList.addAll(restTime(date, meter, reportWorkDTO));
        return resList;
    }

    /**
     * 获取报工的空闲时间
     *
     * @param date          班次日期
     * @param meter         仪表
     * @param reportWorkDTO 报工信息
     * @return
     */
    private List<DwConditionDTO> restTime(LocalDate date, EpDwMeterPartitionDTO meter, List<DwCurrentReportWorkDTO> reportWorkDTO) {
        //定义返回集合
        List<DwConditionDTO> resList = Lists.newArrayList();
        //早班开始时间
        LocalDateTime morningLdt = LocalDateUtil.getMorning(date);
        //晚班开始时间
        LocalDateTime nightLdt = LocalDateUtil.getNight(date);
        //晚班结束时间
        LocalDateTime lastLdt = LocalDateUtil.getMorning(date.plusDays(1));
        //按照时间排序
        reportWorkDTO.sort(Comparator.comparing(DwCurrentReportWorkDTO::getStart));
        //考虑数据重叠，去重
        List<DwCurrentReportWorkDTO> filterList = Lists.newArrayList();
        for (DwCurrentReportWorkDTO reportWork : reportWorkDTO) {
            long count = reportWorkDTO.stream().filter(v -> v != reportWork)
                    .filter(v -> (v.getStart().isBefore(reportWork.getStart()) && v.getEnd().isAfter(reportWork.getEnd()))).count();
            if (count == 0) {
                filterList.add(reportWork);
            }
        }
        //插入值缝隙时间
        for (int i = 1; i < filterList.size(); i++) {
            DwCurrentReportWorkDTO prev = reportWorkDTO.get(i - 1);
            DwCurrentReportWorkDTO cur = reportWorkDTO.get(i);
            //2个时间同属于同一个班次，才可以做缝隙
            if (cur.getStart().isAfter(prev.getEnd()) && CalculateDateUtil.isSameClassType(cur.getStart(), prev.getEnd())) {
                DwConditionDTO dwConditionDTO = getDefaultDwConditionDTO(meter, prev.getEnd(), cur.getStart(), cur.getClassDate(), cur.getClassId());
                //添加空闲时间
                resList.add(dwConditionDTO);
            }
        }
        resList.addAll(calPrevAntTailTime(meter, reportWorkDTO, morningLdt, nightLdt, QtManagerConstant.QtClassesEnum.DAY.getCode()));
        resList.addAll(calPrevAntTailTime(meter, reportWorkDTO, nightLdt, lastLdt, QtManagerConstant.QtClassesEnum.NIGHT.getCode()));
        return resList;
    }

    /**
     * 计算报工前后的缝隙时间
     *
     * @param reportWorkDTO 当前的报工记录
     * @param start         开始时间
     * @param end           结束时间
     * @return 前后缝隙时间
     */
    private List<DwConditionDTO> calPrevAntTailTime(EpDwMeterPartitionDTO meter, List<DwCurrentReportWorkDTO> reportWorkDTO, LocalDateTime start, LocalDateTime end, Integer code) {
        //存在2个元素的情况： 开始时间大于8点  结束时间小于20点
        //存在1个元素的情况： 报工开始时间等于班次开始时间 或者 报工结束时间等于班次结束时间
        //不存在元素的情况:   班次时间没有报工

        //定义返回集合
        List<DwConditionDTO> resList = Lists.newArrayList();
        //1.确定start -end 时间段内有没有报工
        long count = reportWorkDTO.stream().filter(v -> (v.getStart().isAfter(start) || v.getStart().isEqual(start)) && (v.getEnd().isBefore(end) || v.getEnd().isEqual(end))).count();
        //2.没有元素，班次时间没有报工
        if (count == 0) {
            resList.add(getDefaultDwConditionDTO(meter, start, end, start.toLocalDate(), code));
        } else {
            //存在元素①开始和结束都有元素 ②开始有或者结束有 ③开始结束都没有【班次日期都在报工】
            //开始时间必须大于start 并且结束时间必须小于等于end
            Optional<DwCurrentReportWorkDTO> first = reportWorkDTO.stream().filter(v -> (v.getStart().isAfter(start) || v.getStart().isEqual(start)) && (end.isAfter(v.getEnd()) || end.isEqual(v.getEnd()))).min(Comparator.comparing(DwCurrentReportWorkDTO::getStart));
            //开始时间必须大于等于start 并且结束时间必须小于end
            Optional<DwCurrentReportWorkDTO> last = reportWorkDTO.stream().filter(v -> (v.getStart().isAfter(start) || v.getStart().isEqual(start)) && (v.getEnd().isBefore(end) || end.isEqual(v.getEnd()))).max(Comparator.comparing(DwCurrentReportWorkDTO::getEnd));
            if (first.isPresent() && !first.get().getStart().isEqual(start)) {
                resList.add(getDefaultDwConditionDTO(meter, start, first.get().getStart(), start.toLocalDate(), code));
            }
            if (last.isPresent() && !last.get().getEnd().isEqual(end)) {
                resList.add(getDefaultDwConditionDTO(meter, last.get().getEnd(), end, start.toLocalDate(), code));
            }
        }
        return resList;
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
        dwConditionDTO.setReportWorkId(null);
        dwConditionDTO.setThick(null);
        dwConditionDTO.setClassId(classId);
        return dwConditionDTO;
    }

    /**
     * 构造元素
     *
     * @param meter 仪表信息
     * @param data  报工数据
     * @return
     */
    private DwConditionDTO getDwConditionDTO(EpDwMeterPartitionDTO meter, DwCurrentReportWorkDTO data) {
        DwConditionDTO dwConditionDTO = new DwConditionDTO();
        dwConditionDTO.setMeterId(meter.getMeterId());
        dwConditionDTO.setCode(meter.getDistantMapCode());
        dwConditionDTO.setReadDate(data.getClassDate());
        dwConditionDTO.setClassId(data.getClassId());
        dwConditionDTO.setThick(data.getThick());
        dwConditionDTO.setReportWorkId(data.getReportWorkId());
        dwConditionDTO.setStart(data.getStart());
        dwConditionDTO.setEnd(data.getEnd());
        return dwConditionDTO;
    }
}
