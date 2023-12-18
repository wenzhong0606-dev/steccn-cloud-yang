package com.steccn.pms.service.impl;

import cn.hutool.core.lang.Assert;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.StackGraphDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.*;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.DashboardNumDTO;
import com.steccn.pms.service.PmsPullForceService;
import com.steccn.pms.service.PmsReportDashboardCtService;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.pms.vo.LineProduceClassGroupVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.XAxisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
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

    @Override
    public QtLineChartBarDTO yieldRateDate(Integer workShopId, LocalDate start, LocalDate end) {
        List<LocalDate> timeList = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), start, end);
        Assert.notEmpty(timeList, "暂无时间窗口");
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notNull(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());

        Map<String, BigDecimal> pmMap = reportWorkService.datePmUnit(lineIdList, start, end, 0, PmManagerConstant.MEASURE_UNIT_TON);
        Map<String, BigDecimal> inStorageMap = reportWorkService.dateInStorageUnit(lineIdList, start, end, PmManagerConstant.MEASURE_UNIT_TON);
        Map<String, BigDecimal> forceMap = pmsPullForceService.datePullForce(lineIdList, start, end);

        //定义数据集合
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        for (int i = 1; i < timeList.size(); i++) {
            LocalDate classDate = timeList.get(i - 1);
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            stackGraphDTO.setXAxis(XAxisUtil.convert(QtManagerConstant.QtGranularityEnum.DAY, classDate));
            List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
            //产量，入库，耗用
            dataList.add(new TupleDTO<>("完工总成品率(%)", BigDecimalUtil.percent(pmMap.get(classDate.toString()), forceMap.get(classDate.toString()))));
            dataList.add(new TupleDTO<>("入库总成品率(%)", BigDecimalUtil.percent(inStorageMap.get(classDate.toString()), forceMap.get(classDate.toString()))));
            stackGraphDTO.setData(dataList);
            stackGraphDTOList.add(stackGraphDTO);
        }
        //自定义转换工具
        return StackGraphUtil.convertData(stackGraphDTOList);
    }

    @Override
    public QtLineChartBarDTO pmDate(Integer workShopId, LocalDate start, LocalDate end, Integer bit, String unit) {
        DashboardNumDTO numDTO = new DashboardNumDTO();
        numDTO.setType(0);
        numDTO.setWorkShopId(workShopId);
        numDTO.setStart(start);
        numDTO.setEnd(end);
        numDTO.setBit(bit);
        numDTO.setUnit(unit);
        return num("累计产量总数", numDTO, (x) -> reportWorkService.datePmUnit(x.getLineIdList(), x.getStart(), x.getEnd(), x.getType(), unit));
    }

    @Override
    public QtLineChartBarDTO inStorageDate(Integer workShopId, LocalDate start, LocalDate end, int bit, String unit) {
        DashboardNumDTO numDTO = new DashboardNumDTO();
        numDTO.setWorkShopId(workShopId);
        numDTO.setStart(start);
        numDTO.setEnd(end);
        numDTO.setBit(bit);
        numDTO.setUnit(unit);
        return num("累计入库总数量", numDTO, (x) -> reportWorkService.dateInStorageUnit(x.getLineIdList(), x.getStart(), x.getEnd(), unit));
    }

    @Override
    public QtLineChartBarDTO pullForceDate(Integer workShopId, LocalDate start, LocalDate end, int bit, String unit) {
        DashboardNumDTO numDTO = new DashboardNumDTO();
        numDTO.setWorkShopId(workShopId);
        numDTO.setStart(start);
        numDTO.setEnd(end);
        numDTO.setBit(bit);
        numDTO.setUnit(unit);
        return num("累计拉引量总数", numDTO, (x) -> pmsPullForceService.datePullForce(x.getLineIdList(), x.getStart(), x.getEnd()));
    }

    @Override
    public QtLineChartBarDTO yieldRateDatePm(Integer workShopId, LocalDate start, LocalDate end, Integer dateType) {
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
    public QtLineChartBarDTO yieldRateDateInStorage(Integer workShopId, LocalDate start, LocalDate end, Integer dateType) {
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
    public QtLineChartBarDTO lineDatePm(Integer workShopId, LocalDate start, LocalDate end, Integer dateType) {
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
    public QtLineChartBarDTO lineDateInStorage(Integer workShopId, LocalDate start, LocalDate end, Integer dateType) {
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
    public QtLineChartBarDTO lineDatePf(Integer workShopId, LocalDate start, LocalDate end, Integer dateType) {
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
    private QtLineChartBarDTO dateQuantity(List<SysOfficeVO> wsLineList, List<LocalDate> timeList, Map<String, BigDecimal> numMap, String hintCh,QtManagerConstant.QtGranularityEnum granularity) {
        //定义数据集合
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
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
                dataList.add(new TupleDTO<>(line.getOfficeName() + hintCh, lineBd));
            });
            stackGraphDTO.setData(dataList);
            stackGraphDTOList.add(stackGraphDTO);
        }
        QtLineChartBarDTO qtLineChartBarDTO = StackGraphUtil.convertData(stackGraphDTOList);
        return qtLineChartBarDTO;
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
    private QtLineChartBarDTO dateClassQuantity(List<SysOfficeVO> wsLineList, List<LocalDate> timeList, Map<String, BigDecimal> numMap, String hintCh) {
        //定义数据集合
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        List<String> xAxisList1 = Lists.newArrayList();
        List<String> xAxisList2 = Lists.newArrayList();
        List<SysDictVO> sysDictVOList = FeignOptional.of(() -> remoteDictFeignApi.listByType(PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
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
                    dataList.add(new TupleDTO<>(line.getOfficeName() + hintCh, lineBd));
                });
                stackGraphDTO.setData(dataList);
                stackGraphDTOList.add(stackGraphDTO);
            }
        }
        QtLineChartBarDTO qtLineChartBarDTO = StackGraphUtil.convertData(stackGraphDTOList);
        qtLineChartBarDTO.setXAxis(new LineProduceClassGroupVO(xAxisList1, xAxisList2));
        qtLineChartBarDTO.setXData(null);
        return qtLineChartBarDTO;
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
    public QtLineChartBarDTO dateRate(List<SysOfficeVO> wsLineList, List<LocalDate> timeList, Map<String, BigDecimal> map1, Map<String, BigDecimal> map2,QtManagerConstant.QtGranularityEnum granularity) {
        //定义数据集合
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
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
                }
                dataList.add(new TupleDTO<>(line.getOfficeName() + "完工成品率", BigDecimalUtil.percent(lineBd1, lineBd2)));
            });
            stackGraphDTO.setData(dataList);
            stackGraphDTOList.add(stackGraphDTO);
        }
        QtLineChartBarDTO qtLineChartBarDTO = StackGraphUtil.convertData(stackGraphDTOList);
        return qtLineChartBarDTO;
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
    public QtLineChartBarDTO dateClassRate(List<SysOfficeVO> wsLineList, List<LocalDate> timeList, Map<String, BigDecimal> map1, Map<String, BigDecimal> map2) {
        //定义数据集合
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        List<String> xAxisList1 = Lists.newArrayList();
        List<String> xAxisList2 = Lists.newArrayList();
        List<SysDictVO> sysDictVOList = FeignOptional.of(() -> remoteDictFeignApi.listByType(PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
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
                    dataList.add(new TupleDTO<>(line.getOfficeName() + "完工成品率", BigDecimalUtil.percent(lineBd1, lineBd2)));
                });
                stackGraphDTO.setData(dataList);
                stackGraphDTOList.add(stackGraphDTO);
            }
        }
        QtLineChartBarDTO qtLineChartBarDTO = StackGraphUtil.convertData(stackGraphDTOList);
        qtLineChartBarDTO.setXAxis(new LineProduceClassGroupVO(xAxisList1, xAxisList2));
        qtLineChartBarDTO.setXData(null);
        return qtLineChartBarDTO;
    }

    /**
     * 班次产量，入库量，拉引量
     * *
     *
     * @param numDTO   条件
     * @param function 函数
     * @return
     */
    private QtLineChartBarDTO num(String hint, DashboardNumDTO numDTO, Function<DashboardNumDTO, Map<String, BigDecimal>> function) {
        List<LocalDate> timeList = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), numDTO.getStart(), numDTO.getEnd());
        Assert.notEmpty(timeList, "暂无时间窗口");
        List<SysOfficeVO> wsLineList = getWsLineList(numDTO.getWorkShopId());
        Assert.notNull(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        numDTO.setLineIdList(lineIdList);
        Map<String, BigDecimal> pmMap = function.apply(numDTO);

        //定义数据集合
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        for (int i = 1; i < timeList.size(); i++) {
            LocalDate classDate = timeList.get(i - 1);
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            stackGraphDTO.setXAxis(XAxisUtil.convert(QtManagerConstant.QtGranularityEnum.DAY, classDate));
            List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
            //产量，入库，耗用
            dataList.add(new TupleDTO<>(hint, BigDecimalUtil.setScale(pmMap.get(classDate.toString()), numDTO.getBit())));
            stackGraphDTO.setData(dataList);
            stackGraphDTOList.add(stackGraphDTO);
        }
        //自定义转换工具
        return StackGraphUtil.convertData(stackGraphDTOList);
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
