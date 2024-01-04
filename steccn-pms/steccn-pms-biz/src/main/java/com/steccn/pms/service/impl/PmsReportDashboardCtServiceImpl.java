package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.*;
import com.steccn.common.util.*;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.dp.feign.RemoteDpFeignApi;
import com.steccn.ems.feign.RemoteMeterReadFeignApi;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.DashboardNumDTO;
import com.steccn.pms.dto.QtLineChartBarCountDTO;
import com.steccn.pms.service.PmsPullForceService;
import com.steccn.pms.service.PmsReportDashboardCtService;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.pms.vo.JpCountReportDetailRowVO;
import com.steccn.pms.vo.JpCountReportDetailVO;
import com.steccn.pms.vo.LineProduceClassGroupVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.MysqlDateKeyUtil;
import com.steccn.qms.util.XAxisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 基片车间生产驾驶舱-统计看板服务实现
 * @date 2023/9/11
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PmsReportDashboardCtServiceImpl implements PmsReportDashboardCtService {

    private final PmsReportWorkService reportWorkService;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final PmsPullForceService pmsPullForceService;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final RemoteMeterReadFeignApi remoteMeterReadFeignApi;
    private final RemoteDpFeignApi remoteDpFeignApi;

    @Override
    public QtLineChartBarCountDTO yieldRateDate(Integer workShopId, LocalDate start, LocalDate end, Integer granularity) {
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByCode(granularity);
        Assert.notNull(granularityEnum, "粒度不存在");
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notNull(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        if (QtManagerConstant.QtGranularityEnum.CLASS.equals(granularityEnum)) {
            List<LocalDate> timeList = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), start, end);
            Assert.notEmpty(timeList, "暂无时间窗口");
            Map<String, BigDecimal> pmMap = reportWorkService.dateClassPmUnit(lineIdList, start, end, 0, PmManagerConstant.MEASURE_UNIT_TON);
            Map<String, BigDecimal> inStorageMap = reportWorkService.dateClassInStorageUnit(lineIdList, start, end, PmManagerConstant.MEASURE_UNIT_TON);
            Map<String, BigDecimal> forceMap = pmsPullForceService.dateClassPullForce(lineIdList, start, end);
            //获取班次
            List<SysDictVO> sysDictVOList = FeignOptional.of(() -> remoteDictFeignApi.listByType(PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //定义数据集合
            List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
            List<String> xAxisList1 = Lists.newArrayList();
            List<String> xAxisList2 = Lists.newArrayList();
            List<BigDecimal> countPmValueList = Lists.newArrayList();
            List<BigDecimal> countInStorageValueList = Lists.newArrayList();
            List<BigDecimal> countForceValueList = Lists.newArrayList();
            for (int i = 1; i < timeList.size(); i++) {
                LocalDate classDate = timeList.get(i - 1);
                xAxisList1.add(XAxisUtil.convert(QtManagerConstant.QtGranularityEnum.CLASS, classDate));
                sysDictVOList.forEach(ele -> {
                    xAxisList2.add(ele.getName());
                    StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
                    stackGraphDTO.setXAxis(XAxisUtil.convert(QtManagerConstant.QtGranularityEnum.CLASS, classDate));
                    List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
                    //产量，入库，耗用
                    countPmValueList.add(pmMap.get(classDate + "_" + ele.getId()));
                    countInStorageValueList.add(inStorageMap.get(classDate + "_" + ele.getId()));
                    countForceValueList.add(forceMap.get(classDate + "_" + ele.getId()));
                    dataList.add(new TupleDTO<>("完工总成品率(%)", BigDecimalUtil.percent(pmMap.get(classDate + "_" + ele.getId()), forceMap.get(classDate + "_" + ele.getId()))));
                    dataList.add(new TupleDTO<>("入库总成品率(%)", BigDecimalUtil.percent(inStorageMap.get(classDate + "_" + ele.getId()), forceMap.get(classDate + "_" + ele.getId()))));
                    stackGraphDTO.setData(dataList);
                    stackGraphDTOList.add(stackGraphDTO);
                });
            }
            //自定义转换工具
            QtLineChartBarDTO qtLineChartBarDTO = StackGraphUtil.convertData(stackGraphDTOList);
            qtLineChartBarDTO.setXAxis(new LineProduceClassGroupVO(xAxisList1, xAxisList2));
            qtLineChartBarDTO.setXData(null);
            QtLineChartBarCountDTO resDto = new QtLineChartBarCountDTO();
            resDto.setQtLineChartBarDTO(qtLineChartBarDTO);
            //统计信息：选择时间内，报工/入库总数汇总 / 拉引量汇总，显示“总入库成品率、完工成品率”
            resDto.setTotalRate(BigDecimalUtil.percent(BigDecimalUtil.sum(countPmValueList), BigDecimalUtil.sum(countForceValueList)));
            resDto.setTotalQuantity(BigDecimalUtil.percent(BigDecimalUtil.sum(countInStorageValueList), BigDecimalUtil.sum(countForceValueList)));
            return resDto;
        }else {
            List<LocalDate> timeList = ShardTimeUtil.cutDate(granularityEnum.getType(), start, end);
            Assert.notEmpty(timeList, "暂无时间窗口");
            Map<String, BigDecimal> pmMap = reportWorkService.datePmUnit(lineIdList, start, end, 0, PmManagerConstant.MEASURE_UNIT_TON);
            Map<String, BigDecimal> inStorageMap = reportWorkService.dateInStorageUnit(lineIdList, start, end, PmManagerConstant.MEASURE_UNIT_TON);
            Map<String, BigDecimal> forceMap = pmsPullForceService.datePullForce(lineIdList, start, end);
            //定义数据集合
            List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
            List<BigDecimal> countPmValueList = Lists.newArrayList();
            List<BigDecimal> countInStorageValueList = Lists.newArrayList();
            List<BigDecimal> countForceValueList = Lists.newArrayList();
            for (int i = 1; i < timeList.size(); i++) {
                LocalDate classDate = timeList.get(i - 1);
                LocalDate classDateEnd = timeList.get(i);
                List<BigDecimal> pmValueList = Lists.newArrayList();
                List<BigDecimal> inStorageValueList = Lists.newArrayList();
                List<BigDecimal> forceValueList = Lists.newArrayList();
                for (LocalDate ldt = classDate; LocalDateUtil.lt(ldt, classDateEnd); ldt = ldt.plusDays(1)) {
                    pmValueList.add(pmMap.get(ldt.toString()));
                    inStorageValueList.add(inStorageMap.get(ldt.toString()));
                    forceValueList.add(forceMap.get(ldt.toString()));
                    countPmValueList.add(pmMap.get(ldt.toString()));
                    countInStorageValueList.add(inStorageMap.get(ldt.toString()));
                    countForceValueList.add(forceMap.get(ldt.toString()));
                }
                StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
                stackGraphDTO.setXAxis(XAxisUtil.convert(granularityEnum, classDate));
                List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
                //产量，入库，耗用
                BigDecimal forceValue = BigDecimalUtil.sum(forceValueList);
                dataList.add(new TupleDTO<>("完工总成品率(%)", BigDecimalUtil.percent(BigDecimalUtil.sum(pmValueList), forceValue)));
                dataList.add(new TupleDTO<>("入库总成品率(%)", BigDecimalUtil.percent(BigDecimalUtil.sum(inStorageValueList), forceValue)));
                stackGraphDTO.setData(dataList);
                stackGraphDTOList.add(stackGraphDTO);
            }
            //自定义转换工具
            QtLineChartBarDTO qtLineChartBarDTO = StackGraphUtil.convertData(stackGraphDTOList);
            QtLineChartBarCountDTO resDto = new QtLineChartBarCountDTO();
            resDto.setQtLineChartBarDTO(qtLineChartBarDTO);
            //统计信息：选择时间内，报工/入库总数汇总 / 拉引量汇总，显示“总入库成品率、完工成品率”
            resDto.setTotalRate(BigDecimalUtil.percent(BigDecimalUtil.sum(countPmValueList), BigDecimalUtil.sum(countForceValueList)));
            resDto.setTotalQuantity(BigDecimalUtil.percent(BigDecimalUtil.sum(countInStorageValueList), BigDecimalUtil.sum(countForceValueList)));
            return resDto;
        }
    }

    @Override
    public QtLineChartBarCountDTO pmDate(Integer workShopId, LocalDate start, LocalDate end, Integer granularity, Integer bit, String unit) {
        DashboardNumDTO numDTO = new DashboardNumDTO();
        numDTO.setType(0);
        numDTO.setWorkShopId(workShopId);
        numDTO.setStart(start);
        numDTO.setEnd(end);
        numDTO.setBit(bit);
        numDTO.setUnit(unit);
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByCode(granularity);
        Assert.notNull(granularityEnum, "粒度不存在");
        if (QtManagerConstant.QtGranularityEnum.CLASS.equals(granularityEnum)) {
            return numClass("累计产量总数", numDTO, (x) -> reportWorkService.dateClassPmUnit(x.getLineIdList(), x.getStart(), x.getEnd(), x.getType(), unit));
        } else {
            return num("累计产量总数", numDTO, (x) -> reportWorkService.datePmUnit(x.getLineIdList(), x.getStart(), x.getEnd(), x.getType(), unit), granularityEnum);
        }
    }

    @Override
    public QtLineChartBarCountDTO inStorageDate(Integer workShopId, LocalDate start, LocalDate end, Integer granularity, int bit, String unit) {
        DashboardNumDTO numDTO = new DashboardNumDTO();
        numDTO.setWorkShopId(workShopId);
        numDTO.setStart(start);
        numDTO.setEnd(end);
        numDTO.setBit(bit);
        numDTO.setUnit(unit);
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByCode(granularity);
        Assert.notNull(granularityEnum, "粒度不存在");
        if (QtManagerConstant.QtGranularityEnum.CLASS.equals(granularityEnum)) {
            return numClass("累计入库总数量", numDTO, (x) -> reportWorkService.dateClassInStorageUnit(x.getLineIdList(), x.getStart(), x.getEnd(), unit));
        } else {
            return num("累计入库总数量", numDTO, (x) -> reportWorkService.dateInStorageUnit(x.getLineIdList(), x.getStart(), x.getEnd(), unit), granularityEnum);
        }
    }

    @Override
    public QtLineChartBarCountDTO pullForceDate(Integer workShopId, LocalDate start, LocalDate end, Integer granularity, int bit, String unit) {
        DashboardNumDTO numDTO = new DashboardNumDTO();
        numDTO.setWorkShopId(workShopId);
        numDTO.setStart(start);
        numDTO.setEnd(end);
        numDTO.setBit(bit);
        numDTO.setUnit(unit);
        QtManagerConstant.QtGranularityEnum granularityEnum = QtManagerConstant.QtGranularityEnum.getByCode(granularity);
        Assert.notNull(granularityEnum, "粒度不存在");
        if (QtManagerConstant.QtGranularityEnum.CLASS.equals(granularityEnum)) {
            return numClass("累计拉引量总数", numDTO, (x) -> pmsPullForceService.dateClassPullForce(x.getLineIdList(), x.getStart(), x.getEnd()));
        } else {
            return num("累计拉引量总数", numDTO, (x) -> pmsPullForceService.datePullForce(x.getLineIdList(), x.getStart(), x.getEnd()), granularityEnum);
        }
    }

    @Override
    public QtLineChartBarCountDTO yieldRateDatePm(Integer workShopId, LocalDate start, LocalDate end, Integer dateType) {
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dateType);
        Assert.notNull(granularity, "日期粒度不存在");
        //根据车间获取产线
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notEmpty(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        if (QtManagerConstant.QtGranularityEnum.CLASS.equals(granularity)) {
            List<LocalDate> timeList = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), start, end);
            Assert.notEmpty(timeList, "日期选择有误");
            //根据日期区间获取班次+日期+产线的产量
            Map<String, BigDecimal> numMap = reportWorkService.dateClassLinePmUnit(lineIdList, start, end, 0, PmManagerConstant.MEASURE_UNIT_TON);
            Map<String, BigDecimal> pfMap = pmsPullForceService.dateClassLinePmUnit(lineIdList, start, end);
            return dateClassRate(wsLineList, timeList, numMap, pfMap);
        } else {
            List<LocalDate> timeList = ShardTimeUtil.cutDate(granularity.getType(), start, end);
            Assert.notEmpty(timeList, "日期选择有误");
            //根据日期区间获取日期+产线的产量
            Map<String, BigDecimal> numMap = reportWorkService.dateLinePmUnit(lineIdList, start, end, 0, PmManagerConstant.MEASURE_UNIT_TON);
            Map<String, BigDecimal> pfMap = pmsPullForceService.dateLinePmUnit(lineIdList, start, end);
            return dateRate(wsLineList, timeList, numMap, pfMap, granularity);
        }
    }

    @Override
    public QtLineChartBarCountDTO yieldRateDateInStorage(Integer workShopId, LocalDate start, LocalDate end, Integer dateType) {
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dateType);
        Assert.notNull(granularity, "日期粒度不存在");
        //根据车间获取产线
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notEmpty(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        if (QtManagerConstant.QtGranularityEnum.CLASS.equals(granularity)) {
            List<LocalDate> timeList = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), start, end);
            Assert.notEmpty(timeList, "日期选择有误");
            //根据日期区间获取班次+日期+产线的产量
            Map<String, BigDecimal> numMap = reportWorkService.dateClassLineInStorageUnit(lineIdList, start, end, PmManagerConstant.MEASURE_UNIT_TON);
            Map<String, BigDecimal> pfMap = pmsPullForceService.dateClassLinePmUnit(lineIdList, start, end);
            return dateClassRate(wsLineList, timeList, numMap, pfMap);
        } else {
            List<LocalDate> timeList = ShardTimeUtil.cutDate(granularity.getType(), start, end);
            Assert.notEmpty(timeList, "日期选择有误");
            //根据日期区间获取日期+产线的产量
            Map<String, BigDecimal> numMap = reportWorkService.dateLineInStorageUnit(lineIdList, start, end, PmManagerConstant.MEASURE_UNIT_TON);
            Map<String, BigDecimal> pfMap = pmsPullForceService.dateLinePmUnit(lineIdList, start, end);
            return dateRate(wsLineList, timeList, numMap, pfMap, granularity);
        }
    }

    @Override
    public QtLineChartBarCountDTO lineDatePm(Integer workShopId, LocalDate start, LocalDate end, Integer dateType) {
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dateType);
        Assert.notNull(granularity, "日期粒度不存在");
        //根据车间获取产线
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notEmpty(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        if (QtManagerConstant.QtGranularityEnum.CLASS.equals(granularity)) {
            //根据日期区间获取班次+日期+产线的产量
            Map<String, BigDecimal> numMap = reportWorkService.dateClassLinePmUnit(lineIdList, start, end, 0, PmManagerConstant.MEASURE_UNIT_TON);
            List<LocalDate> timeList = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), start, end);
            Assert.notEmpty(timeList, "日期选择有误");
            return dateClassQuantity(wsLineList, timeList, numMap, "总产量");
        } else {
            //根据日期区间获取日期+产线的产量
            List<LocalDate> timeList = ShardTimeUtil.cutDate(granularity.getType(), start, end);
            Assert.notEmpty(timeList, "日期选择有误");
            Map<String, BigDecimal> numMap = reportWorkService.dateLinePmUnit(lineIdList, start, end, 0, PmManagerConstant.MEASURE_UNIT_TON);
            return dateQuantity(wsLineList, timeList, numMap, "总产量", granularity);
        }
    }

    @Override
    public QtLineChartBarCountDTO lineDateInStorage(Integer workShopId, LocalDate start, LocalDate end, Integer dateType) {
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dateType);
        Assert.notNull(granularity, "日期粒度不存在");
        //根据车间获取产线
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notEmpty(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        if (QtManagerConstant.QtGranularityEnum.CLASS.equals(granularity)) {
            List<LocalDate> timeList = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), start, end);
            Assert.notEmpty(timeList, "日期选择有误");
            //根据日期区间获取班次+日期+产线的产量
            Map<String, BigDecimal> numMap = reportWorkService.dateClassLineInStorageUnit(lineIdList, start, end, PmManagerConstant.MEASURE_UNIT_TON);
            return dateClassQuantity(wsLineList, timeList, numMap, "入库总数");
        } else {
            List<LocalDate> timeList = ShardTimeUtil.cutDate(granularity.getType(), start, end);
            Assert.notEmpty(timeList, "日期选择有误");
            //根据日期区间获取日期+产线的产量
            Map<String, BigDecimal> numMap = reportWorkService.dateLineInStorageUnit(lineIdList, start, end, PmManagerConstant.MEASURE_UNIT_TON);
            return dateQuantity(wsLineList, timeList, numMap, "入库总数",granularity);
        }
    }

    @Override
    public QtLineChartBarCountDTO lineDatePf(Integer workShopId, LocalDate start, LocalDate end, Integer dateType) {
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dateType);
        Assert.notNull(granularity, "日期粒度不存在");

        //根据车间获取产线
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notEmpty(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());

        if (QtManagerConstant.QtGranularityEnum.CLASS.equals(granularity)) {
            List<LocalDate> timeList = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), start, end);
            Assert.notEmpty(timeList, "日期选择有误");
            //根据日期区间获取班次+日期+产线的产量
            Map<String, BigDecimal> numMap = pmsPullForceService.dateClassLinePmUnit(lineIdList, start, end);
            return dateClassQuantity(wsLineList, timeList, numMap, "拉引量总数");
        } else {
            List<LocalDate> timeList = ShardTimeUtil.cutDate(granularity.getType(), start, end);
            Assert.notEmpty(timeList, "日期选择有误");
            //根据日期区间获取日期+产线的产量
            Map<String, BigDecimal> numMap = pmsPullForceService.dateLinePmUnit(lineIdList, start, end);
            return dateQuantity(wsLineList, timeList, numMap, "拉引量总数", granularity);
        }
    }

    @Override
    public JpCountReportDetailVO countReportDetail(Integer workShopId, LocalDate start, LocalDate end, Integer granularityInt) throws ExecutionException, InterruptedException {
        //确定粒度存在
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(granularityInt);
        Assert.notNull(granularity, "粒度不存在");
        List<LocalDate> timeList = ShardTimeUtil.cutDate(granularity.getType(), start, end);
        Assert.notEmpty(timeList, "时间区间不存在");
        JpCountReportDetailVO resVo = new JpCountReportDetailVO();
        List<String> dateStrList = Lists.newArrayList();

        //并行执行
        //获取耗料
        CompletableFuture<List<CommonReportDTO>> hlList = CompletableFuture.supplyAsync(() -> {
            DpPmsOperateDTO  dpPmsOperateDTO = new DpPmsOperateDTO();
            dpPmsOperateDTO.setStart(start);
            dpPmsOperateDTO.setEnd(end);
            dpPmsOperateDTO.setGranularity(granularityInt);
            dpPmsOperateDTO.setOfficeId(workShopId);
            return FeignOptional.of(() -> remoteDpFeignApi.consumeMaterial(dpPmsOperateDTO), FeignConstant.STECCN_EMS_SERVICE_NAME).get();
        }).exceptionally(ex -> Lists.newArrayList());
        CompletableFuture<CommonReportDTO> gasFuture = CompletableFuture.supplyAsync(() -> {
            return FeignOptional.of(() -> remoteMeterReadFeignApi.energyConsume(new Tuple5DTO<>(3, workShopId, start, end, granularityInt)), FeignConstant.STECCN_EMS_SERVICE_NAME).get();
        }).exceptionally(ex -> null);
        CompletableFuture<CommonReportDTO> oilFuture = CompletableFuture.supplyAsync(() -> {
            return FeignOptional.of(() -> remoteMeterReadFeignApi.energyConsume(new Tuple5DTO<>(4, workShopId, start, end, granularityInt)), FeignConstant.STECCN_EMS_SERVICE_NAME).get();
        }).exceptionally(ex -> null);
        CompletableFuture<CommonReportDTO> kcalFuture = CompletableFuture.supplyAsync(() -> {
            return FeignOptional.of(() -> remoteMeterReadFeignApi.energyConsume(new Tuple5DTO<>(5, workShopId, start, end, granularityInt)), FeignConstant.STECCN_EMS_SERVICE_NAME).get();
        }).exceptionally(ex -> null);
        CompletableFuture<CommonReportDTO> electricFuture = CompletableFuture.supplyAsync(() -> {
            return FeignOptional.of(() -> remoteMeterReadFeignApi.energyConsume(new Tuple5DTO<>(1, workShopId, start, end, granularityInt)), FeignConstant.STECCN_EMS_SERVICE_NAME).get();
        }).exceptionally(ex -> null);
        CompletableFuture<CommonReportDTO> waterFuture = CompletableFuture.supplyAsync(() -> {
            return FeignOptional.of(() -> remoteMeterReadFeignApi.energyConsume(new Tuple5DTO<>(2, workShopId, start, end, granularityInt)), FeignConstant.STECCN_EMS_SERVICE_NAME).get();
        }).exceptionally(ex -> null);
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf(gasFuture, oilFuture, kcalFuture, electricFuture, waterFuture);
        completableFuture.isDone();
        List<JpCountReportDetailRowVO> rowList = Lists.newArrayList();

        countReportDetailRow(rowList, "耗料", hlList.get(), Boolean.TRUE, granularity, timeList);
        countReportDetailRow(rowList, "燃料", Lists.newArrayList(gasFuture.get(), oilFuture.get(), kcalFuture.get()), Boolean.TRUE, granularity, timeList);
        countReportDetailRow(rowList, "水电", Lists.newArrayList(electricFuture.get(), waterFuture.get()), Boolean.FALSE, granularity, timeList);
        //获取余热发电
        CompletableFuture<List<CommonReportDTO>> heatPowerList = CompletableFuture.supplyAsync(() -> {
            return FeignOptional.of(() -> remoteMeterReadFeignApi.heatPower(new TripleDTO<>(start, end, granularityInt)), FeignConstant.STECCN_EMS_SERVICE_NAME).get();
        }).exceptionally(ex -> Lists.newArrayList());
        countReportDetailRow(rowList, "余热发电", heatPowerList.get(), Boolean.FALSE, granularity, timeList);

        //获取光伏发电
        CompletableFuture<List<CommonReportDTO>> gfList = CompletableFuture.supplyAsync(() -> {
            return FeignOptional.of(() -> remoteMeterReadFeignApi.gfPowerList(new TripleDTO<>(start, end, granularityInt)), FeignConstant.STECCN_EMS_SERVICE_NAME).get();
        }).exceptionally(ex -> Lists.newArrayList());
        countReportDetailRow(rowList, "光伏发电", gfList.get(), Boolean.FALSE, granularity, timeList);

        //获取脱硫脱硝
        for (int i = 1; i < timeList.size(); i++) {
            LocalDate areaStart = timeList.get(i - 1);
            dateStrList.add(XAxisUtil.convert(granularity, areaStart));
        }
        resVo.setDateStrList(dateStrList);
        resVo.setRowList(rowList);
        return resVo;
    }

    /**
     * 获取类别数据
     *
     * @param rowList     返回结果
     * @param category    类型
     * @param dataRowList 数据行
     * @param ctFlag      是否需要合计行
     * @param granularity 粒度
     * @param timeList    时间区间
     */
    private void countReportDetailRow(List<JpCountReportDetailRowVO> rowList, String category, List<CommonReportDTO> dataRowList, Boolean ctFlag, QtManagerConstant.QtGranularityEnum granularity, List<LocalDate> timeList) {
        if (CollUtil.isNotEmpty(dataRowList)) {
            Map<Integer, BigDecimal> ctDataMap = Maps.newLinkedHashMap();
            for (CommonReportDTO ele : dataRowList) {
                if (Objects.nonNull(ele)) {
                    JpCountReportDetailRowVO row = new JpCountReportDetailRowVO();
                    List<BigDecimal> valueList = Lists.newArrayList();
                    row.setValueList(valueList);
                    row.setCategory(category);
                    row.setName(ele.getName());
                    row.setUnitName(ele.getUnit());
                    row.setType(0);
                    rowList.add(row);
                    for (int i = 1; i < timeList.size(); i++) {
                        LocalDate areaStart = timeList.get(i - 1);
                        String convert = MysqlDateKeyUtil.convert(granularity, areaStart);
                        BigDecimal value = ele.getValueMap().get(convert);
                        valueList.add(value);
                        //需要考虑小计
                        if (ctFlag) {
                            MapUtil.add(ctDataMap, i - 1, value, (x, y) -> BigDecimalUtil.add(x, y));
                        }
                    }
                    row.setRowCount(BigDecimalUtil.sum(valueList));
                }
            }
            //如果有小计
            if (ctFlag) {
                JpCountReportDetailRowVO ctRow = new JpCountReportDetailRowVO();
                ctRow.setCategory(category);
                ctRow.setType(1);
                ctRow.setName("小计");
                //计算合计
                Collection<BigDecimal> values = ctDataMap.values();
                ctRow.setValueList(values.stream().collect(Collectors.toList()));
                ctRow.setRowCount(BigDecimalUtil.sum(values));
                rowList.add(ctRow);
            }
        }
    }

    /**
     * 班次日期数量
     *
     * @param wsLineList 产线集合
     * @param timeList   时间集合
     * @param numMap     数据map
     * @param hintCh     提示语
     * @param granularity 粒度
     * @return
     */
    private QtLineChartBarCountDTO dateQuantity(List<SysOfficeVO> wsLineList, List<LocalDate> timeList, Map<String, BigDecimal> numMap, String hintCh,QtManagerConstant.QtGranularityEnum granularity) {
        //定义数据集合
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        //统计信息：选择时间内，拉引量汇总，显示“x线总拉引量”
        //统计信息：选择时间内，报工汇总，显示“总入库产量”
        //统计信息：选择时间内，报工汇总，显示“x线总报工产量”
        Map<String,BigDecimal> countLineMap = Maps.newLinkedHashMap();
        for (int i = 1; i < timeList.size(); i++) {
            LocalDate classDate = timeList.get(i - 1);
            LocalDate end = timeList.get(i);
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            String convert = XAxisUtil.convert(granularity, classDate);
            stackGraphDTO.setXAxis(convert);
            List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
            wsLineList.forEach(line -> {
                BigDecimal lineBd;
                List<BigDecimal> lineBdList = Lists.newArrayList();
                //从classDate ~ end 之间的遍历
                for (LocalDate ldt = classDate; LocalDateUtil.lt(ldt, end); ldt = ldt.plusDays(1)) {
                    lineBdList.add(numMap.get(JointUtil.joint(JointUtil.SEPARATOR_UNDERLINE, line.getId(), ldt)));
                }
                lineBd = BigDecimalUtil.sum(lineBdList);
                lineBd = BigDecimalUtil.setScale(lineBd, UnitLengthUtil.UNIT_TON_LENGTH);
                MapUtil.add(countLineMap, line.getOfficeName(), lineBd, (x, y) -> BigDecimalUtil.add(x, y, UnitLengthUtil.UNIT_TON_LENGTH));
                dataList.add(new TupleDTO<>(line.getOfficeName() + hintCh, lineBd));
            });
            stackGraphDTO.setData(dataList);
            stackGraphDTOList.add(stackGraphDTO);
        }
        QtLineChartBarDTO qtLineChartBarDTO = StackGraphUtil.convertData(stackGraphDTOList);
        QtLineChartBarCountDTO resDto = new QtLineChartBarCountDTO();
        resDto.setQtLineChartBarDTO(qtLineChartBarDTO);
        resDto.setLineQuantity(countLineMap);
        return resDto;
    }

    /**
     * 班次数量
     *
     * @param wsLineList 产线集合
     * @param timeList   时间集合
     * @param numMap     数据map
     * @param hintCh     提示语
     * @return
     */
    private QtLineChartBarCountDTO dateClassQuantity(List<SysOfficeVO> wsLineList, List<LocalDate> timeList, Map<String, BigDecimal> numMap, String hintCh) {
        //定义数据集合
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        List<String> xAxisList1 = Lists.newArrayList();
        List<String> xAxisList2 = Lists.newArrayList();
        List<SysDictVO> sysDictVOList = FeignOptional.of(() -> remoteDictFeignApi.listByType(PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //统计信息：选择时间内，拉引量汇总，显示“x线总拉引量”
        //统计信息：选择时间内，报工汇总，显示“总入库产量”
        //统计信息：选择时间内，报工汇总，显示“x线总报工产量”
        Map<String,BigDecimal> countLineMap = Maps.newLinkedHashMap();
        for (int i = 1; i < timeList.size(); i++) {
            LocalDate classDate = timeList.get(i - 1);
            String convert = XAxisUtil.convert(QtManagerConstant.QtGranularityEnum.DAY, classDate);
            xAxisList1.add(convert);
            //产量，入库，耗用
            for (SysDictVO ele : sysDictVOList) {
                xAxisList2.add(ele.getName());
                List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
                StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
                wsLineList.forEach(line -> {
                    BigDecimal lineBd = numMap.get(JointUtil.joint(JointUtil.SEPARATOR_UNDERLINE, line.getId(), classDate, ele.getId()));
                    lineBd = BigDecimalUtil.setScale(lineBd, UnitLengthUtil.UNIT_TON_LENGTH);
                    MapUtil.add(countLineMap, line.getOfficeName(), lineBd, (x, y) -> BigDecimalUtil.add(x, y, UnitLengthUtil.UNIT_TON_LENGTH));
                    dataList.add(new TupleDTO<>(line.getOfficeName() + hintCh, lineBd));
                });
                stackGraphDTO.setData(dataList);
                stackGraphDTOList.add(stackGraphDTO);
            }
        }
        QtLineChartBarDTO qtLineChartBarDTO = StackGraphUtil.convertData(stackGraphDTOList);
        qtLineChartBarDTO.setXAxis(new LineProduceClassGroupVO(xAxisList1, xAxisList2));
        qtLineChartBarDTO.setXData(null);
        QtLineChartBarCountDTO resDto = new QtLineChartBarCountDTO();
        resDto.setQtLineChartBarDTO(qtLineChartBarDTO);
        resDto.setLineQuantity(countLineMap);
        return resDto;
    }

    /**
     * 日期比率
     *
     * @param wsLineList 产线集合
     * @param timeList   时间集合
     * @param map1       数据map1
     * @param map2       数据map2
     * @return
     */
    public QtLineChartBarCountDTO dateRate(List<SysOfficeVO> wsLineList, List<LocalDate> timeList, Map<String, BigDecimal> map1, Map<String, BigDecimal> map2,QtManagerConstant.QtGranularityEnum granularity) {
        //定义数据集合
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        //统计信息：选择时间内，对应产线报工/入库总数汇总 / 对应拉引量汇总，显示“X线总入库/加工成品率”
        List<BigDecimal> countValueList1 = Lists.newArrayList();
        List<BigDecimal> countValueList2 = Lists.newArrayList();
        Map<String,BigDecimal> countLineMap1 = Maps.newLinkedHashMap();
        Map<String,BigDecimal> countLineMap2 = Maps.newLinkedHashMap();
        for (int i = 1; i < timeList.size(); i++) {
            LocalDate classDate = timeList.get(i - 1);
            LocalDate end = timeList.get(i);
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            String convert = XAxisUtil.convert(granularity, classDate);
            stackGraphDTO.setXAxis(convert);
            List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
            //产量，入库，耗用
            wsLineList.forEach(line -> {
                BigDecimal lineBd1 = null, lineBd2 = null;
                //从classDate ~ end 之间的遍历
                for (LocalDate ldt = classDate; LocalDateUtil.lt(ldt, end); ldt = ldt.plusDays(1)) {
                    String joint = JointUtil.joint(JointUtil.SEPARATOR_UNDERLINE, line.getId(), ldt);
                    lineBd1 = BigDecimalUtil.add(map1.get(joint), lineBd1);
                    lineBd2 = BigDecimalUtil.add(map2.get(joint), lineBd2);
                    MapUtil.add(countLineMap1, line.getOfficeName(), lineBd1, (x, y) -> BigDecimalUtil.add(x, y));
                    MapUtil.add(countLineMap2, line.getOfficeName(), lineBd2, (x, y) -> BigDecimalUtil.add(x, y));
                }
                countValueList1.add(lineBd1);
                countValueList2.add(lineBd2);
                dataList.add(new TupleDTO<>(line.getOfficeName() + "完工成品率", BigDecimalUtil.percent(lineBd1, lineBd2)));
            });
            stackGraphDTO.setData(dataList);
            stackGraphDTOList.add(stackGraphDTO);
        }
        QtLineChartBarDTO qtLineChartBarDTO = StackGraphUtil.convertData(stackGraphDTOList);
        QtLineChartBarCountDTO resDto = new QtLineChartBarCountDTO();
        resDto.setQtLineChartBarDTO(qtLineChartBarDTO);
        Map<String, BigDecimal> countLineMap = Maps.newLinkedHashMap();
        countLineMap1.forEach((k,v) ->{
            countLineMap.put(k, BigDecimalUtil.percent(v, countLineMap2.get(k)));
        });
        resDto.setLineQuantity(countLineMap);
        return resDto;
    }

    /**
     * 班次比率
     *
     * @param wsLineList 产线集合
     * @param timeList   时间集合
     * @param map1       数据map1
     * @param map2       数据map2
     * @return
     */
    public QtLineChartBarCountDTO dateClassRate(List<SysOfficeVO> wsLineList, List<LocalDate> timeList, Map<String, BigDecimal> map1, Map<String, BigDecimal> map2) {
        //定义数据集合
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        List<String> xAxisList1 = Lists.newArrayList();
        List<String> xAxisList2 = Lists.newArrayList();
        List<SysDictVO> sysDictVOList = FeignOptional.of(() -> remoteDictFeignApi.listByType(PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //统计信息：选择时间内，对应产线报工/入库总数汇总 / 对应拉引量汇总，显示“X线总入库/加工成品率”
        Map<String,BigDecimal> countLineMap1 = Maps.newLinkedHashMap();
        Map<String,BigDecimal> countLineMap2 = Maps.newLinkedHashMap();
        for (int i = 1; i < timeList.size(); i++) {
            LocalDate classDate = timeList.get(i - 1);
            String convert = XAxisUtil.convert(QtManagerConstant.QtGranularityEnum.DAY, classDate);
            xAxisList1.add(convert);
            //产量，入库，耗用
            for (SysDictVO ele : sysDictVOList) {
                xAxisList2.add(ele.getName());
                List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
                StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
                wsLineList.forEach(line -> {
                    BigDecimal lineBd1, lineBd2;
                    String joint = JointUtil.joint(JointUtil.SEPARATOR_UNDERLINE, line.getId(), classDate, ele.getId());
                    lineBd1 = map1.get(joint);
                    lineBd2 = map2.get(joint);
                    MapUtil.add(countLineMap1, line.getOfficeName(), lineBd1, (x, y) -> BigDecimalUtil.add(x, y));
                    MapUtil.add(countLineMap2, line.getOfficeName(), lineBd2, (x, y) -> BigDecimalUtil.add(x, y));
                    dataList.add(new TupleDTO<>(line.getOfficeName() + "完工成品率", BigDecimalUtil.percent(lineBd1, lineBd2)));
                });
                stackGraphDTO.setData(dataList);
                stackGraphDTOList.add(stackGraphDTO);
            }
        }
        QtLineChartBarCountDTO resDto = new QtLineChartBarCountDTO();
        QtLineChartBarDTO qtLineChartBarDTO = StackGraphUtil.convertData(stackGraphDTOList);
        qtLineChartBarDTO.setXAxis(new LineProduceClassGroupVO(xAxisList1, xAxisList2));
        qtLineChartBarDTO.setXData(null);
        resDto.setQtLineChartBarDTO(qtLineChartBarDTO);
        Map<String, BigDecimal> countLineMap = Maps.newLinkedHashMap();
        countLineMap1.forEach((k,v) ->{
            countLineMap.put(k, BigDecimalUtil.percent(v, countLineMap2.get(k)));
        });
        resDto.setLineQuantity(countLineMap);
        return resDto;
    }

    /**
     * 产量，入库量，拉引量
     * *
     *
     * @param hint 提示词
     * @param numDTO   条件
     * @param function 函数
     * @param granularityEnum 粒度
     *
     * @return
     */
    private QtLineChartBarCountDTO num(String hint, DashboardNumDTO numDTO, Function<DashboardNumDTO, Map<String, BigDecimal>> function , QtManagerConstant.QtGranularityEnum granularityEnum) {
        List<LocalDate> timeList = ShardTimeUtil.cutDate(granularityEnum.getType(), numDTO.getStart(), numDTO.getEnd());
        Assert.notEmpty(timeList, "暂无时间窗口");
        List<SysOfficeVO> wsLineList = getWsLineList(numDTO.getWorkShopId());
        Assert.notNull(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        numDTO.setLineIdList(lineIdList);
        //根据lambda元素来获取
        Map<String, BigDecimal> numValueMap = function.apply(numDTO);
        //定义数据集合
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        //统计信息：选择时间内，报工汇总，显示“总报工产量”
        //统计信息：选择时间内，报工汇总，显示“总入库产量”
        //统计信息：选择时间内，拉引量汇总，显示“总拉引量”
        List<BigDecimal> countValueList = Lists.newArrayList();
        for (int i = 1; i < timeList.size(); i++) {
            LocalDate classDate = timeList.get(i - 1);
            LocalDate end = timeList.get(i);
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            stackGraphDTO.setXAxis(XAxisUtil.convert(granularityEnum, classDate));
            List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
            List<BigDecimal> valueList = Lists.newArrayList();
            //不同粒度下的聚合
            for (LocalDate ldt = classDate; LocalDateUtil.lt(ldt, end); ldt = ldt.plusDays(1)) {
                valueList.add(numValueMap.get(ldt.toString()));
                countValueList.add(numValueMap.get(ldt.toString()));
            }
            //产量，入库，耗用
            dataList.add(new TupleDTO<>(hint, BigDecimalUtil.setScale(BigDecimalUtil.sum(valueList), numDTO.getBit())));
            stackGraphDTO.setData(dataList);
            stackGraphDTOList.add(stackGraphDTO);
        }
        //自定义转换工具
        QtLineChartBarDTO qtLineChartBarDTO = StackGraphUtil.convertData(stackGraphDTOList);
        QtLineChartBarCountDTO resDto = new QtLineChartBarCountDTO();
        resDto.setQtLineChartBarDTO(qtLineChartBarDTO);
        resDto.setTotalQuantity(BigDecimalUtil.setScale(BigDecimalUtil.sum(countValueList),numDTO.getBit()));
        return resDto;
    }

    /**
     * 产量，入库量，拉引量
     * *
     *
     * @param hint 提示词
     * @param numDTO   条件
     * @param function 函数
     *
     * @return
     */
    private QtLineChartBarCountDTO numClass(String hint, DashboardNumDTO numDTO, Function<DashboardNumDTO, Map<String, BigDecimal>> function) {
        List<LocalDate> timeList = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), numDTO.getStart(), numDTO.getEnd());
        Assert.notEmpty(timeList, "暂无时间窗口");
        List<SysOfficeVO> wsLineList = getWsLineList(numDTO.getWorkShopId());
        Assert.notNull(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        List<SysDictVO> sysDictVOList = FeignOptional.of(() -> remoteDictFeignApi.listByType(PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        numDTO.setLineIdList(lineIdList);
        //根据lambda元素来获取
        Map<String, BigDecimal> numValueMap = function.apply(numDTO);
        //定义数据集合
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        List<String> xAxisList1 = Lists.newArrayList();
        List<String> xAxisList2 = Lists.newArrayList();
        List<BigDecimal> countValueList = Lists.newArrayList();
        for (int i = 1; i < timeList.size(); i++) {
            LocalDate classDate = timeList.get(i - 1);
            String convert = XAxisUtil.convert(QtManagerConstant.QtGranularityEnum.CLASS, classDate);
            xAxisList1.add(convert);
            //遍历班次
            for (SysDictVO ele : sysDictVOList) {
                StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
                stackGraphDTO.setXAxis(convert);
                List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
                xAxisList2.add(ele.getName());
                BigDecimal value = numValueMap.get(classDate.toString() + "_" + ele.getId());
                countValueList.add(value);
                //产量，入库，耗用
                dataList.add(new TupleDTO<>(hint, BigDecimalUtil.setScale(value, numDTO.getBit())));
                stackGraphDTO.setData(dataList);
                stackGraphDTOList.add(stackGraphDTO);
            }
        }
        //自定义转换工具
        QtLineChartBarDTO qtLineChartBarDTO = StackGraphUtil.convertData(stackGraphDTOList);
        qtLineChartBarDTO.setXAxis(new LineProduceClassGroupVO(xAxisList1, xAxisList2));
        qtLineChartBarDTO.setXData(null);
        QtLineChartBarCountDTO resDto = new QtLineChartBarCountDTO();
        resDto.setQtLineChartBarDTO(qtLineChartBarDTO);
        resDto.setTotalQuantity(BigDecimalUtil.setScale(BigDecimalUtil.sum(countValueList),numDTO.getBit()));
        return resDto;
    }

    /**
     * 获取车间的产线集合
     *
     * @param workShopId 车间
     * @return
     */
    private List<SysOfficeVO> getWsLineList(Integer workShopId) {
        //根据车间获取产线集合
        SysOfficeParentDTO sysOfficeParent = new SysOfficeParentDTO();
        sysOfficeParent.setId(workShopId);
        sysOfficeParent.setOfficeType(SysOfficeConstant.PRODUCT_LINE);
        List<SysOfficeVO> lineOfficeList = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(sysOfficeParent), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        return lineOfficeList;
    }
}
