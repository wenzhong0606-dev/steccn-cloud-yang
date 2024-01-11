package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TableHeadColumnDTO;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.util.*;
import com.steccn.common.vo.BaseValueNameVO;
import com.steccn.common.vo.BaseValueVO;
import com.steccn.common.vo.CommonBarLineVO;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.dp.dto.DpPullForceDTO;
import com.steccn.dp.feign.RemoteDpPullForceFeignApi;
import com.steccn.ems.domain.EpEnergyType;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.service.*;
import com.steccn.ems.util.UnitConsumeUtil;
import com.steccn.ems.vo.EpMeterReadAllDataVO;
import com.steccn.ems.vo.PmsOperateEnergyVO;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.dto.PmsOperateDriverDTO;
import com.steccn.pms.feign.RemoteCompleteStorageFeignApi;
import com.steccn.pms.feign.RemoteReportWorkFeignApi;
import com.steccn.pms.vo.SjgCountReportRowVO;
import com.steccn.pms.vo.SjgCountReportVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.MysqlDateKeyUtil;
import com.steccn.qms.util.XAxisUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 能源驾驶舱
 * @date 2023/12/1
 */
@RequiredArgsConstructor
@Service
public class PmsOperateEnergyServiceImpl implements PmsOperateEnergyService {

    private final EpMeterReadService epMeterReadService;
    private final EpPartitionService epPartitionService;
    private final EpEnergyTypeService epEnergyTypeService;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final RemoteDpPullForceFeignApi remoteDpPullForceFeignApi;
    private final RemoteReportWorkFeignApi remoteReportWorkFeignApi;
    private final EmsOfficeManager emsOfficeManager;
    private final RemoteCompleteStorageFeignApi remoteCompleteStorageFeignApi;

    @Override
    public PmsOperateEnergyVO gas(DpPmsOperateDTO dto) {
        return energy(dto, 3, UnitLengthUtil.UNIT_CUBIC_METER_LENGTH);
    }

    @Override
    public PmsOperateEnergyVO oil(DpPmsOperateDTO dto) {
        return energy(dto, 4, UnitLengthUtil.UNIT_TON_LENGTH);
    }

    @Override
    public PmsOperateEnergyVO water(DpPmsOperateDTO dto) {
        return energy(dto, 2, UnitLengthUtil.UNIT_TON_LENGTH);
    }

    @Override
    public PmsOperateEnergyVO electric(DpPmsOperateDTO dto) {
        return energy(dto, 1, UnitLengthUtil.UNIT_CUBIC_METER_LENGTH);
    }

    @Override
    public PmsOperateEnergyVO unitConsume(DpPmsOperateDTO dto) {
        Integer bit = 4;
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
        Assert.notEmpty(dateList, "日期区间有误");
        //考虑是深加工还是基片
        //基片 天然气用量*7800+重油用量*1000*9300)/(拉引量*1000)
        //深加工 电耗/入库产量
//        SysOfficeParentDTO sysOfficeParentDTO = new SysOfficeParentDTO();
//        sysOfficeParentDTO.setId(dto.getOfficeId());
//        sysOfficeParentDTO.setOfficeType(SysOfficeConstant.WORKSHOP);
//        List<SysOfficeVO> sysOfficeVOList = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(sysOfficeParentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<SysOfficeVO> sysOfficeVOList = emsOfficeManager.getChildren(dto.getOfficeId(), SysOfficeConstant.WORKSHOP);
        Assert.notEmpty(sysOfficeVOList, "不存在车间节点");
        Set<Integer> wsIdSet = sysOfficeVOList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        long sjgCount = sysOfficeVOList.stream().filter(ele -> ele.getOfficeName().indexOf("深加工") != -1).count();
        if (sjgCount != 0) {
            //深加工单耗：电耗/入库产量
            return sjgUnitConsume(dto, bit, granularity, dateList);
        } else {
            //基片单耗：天然气用量*7800+重油用量*1000*9300)/(拉引量*1000)
            return jpUnitConsume(dto, bit, granularity, dateList, wsIdSet);
        }
    }

    @Override
    public SjgCountReportVO electricCountReport(Integer workShopId, LocalDate start, LocalDate end) {
        //构建表头
        LocalDate preEnd = start.minusDays(1);
        long daysBetween = ChronoUnit.DAYS.between(start, end);
        LocalDate preStart = preEnd.minusDays(daysBetween);
        SjgCountReportVO reportVO = new SjgCountReportVO();
        reportVO.setColumns(setCountReportHead(start, end, preStart, preEnd));
        //先获取产线
        List<SysOfficeVO> lineList = emsOfficeManager.getWsLineList(workShopId);
        Assert.notEmpty(lineList, "车间下暂无产线");
        //根据产线获取电能节点
        List<Integer> lineIdList = lineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        List<EpPartition> epPartitionList = epPartitionService.list(new LambdaQueryWrapper<EpPartition>().in(EpPartition::getOfficeId, lineIdList).eq(EpPartition::getType, 1));
        Assert.notNull(epPartitionList, "能源树上不存在此节点");
        List<Long> partitionIdList = epPartitionList.stream().map(EpPartition::getId).collect(Collectors.toList());
        //获取本区间电量和上一个区间电量
        Map<Integer, BigDecimal> currentConsumeMap = eleConsume(start, end, partitionIdList);
        Map<Integer, BigDecimal> preConsumeMap = eleConsume(preStart, preEnd, partitionIdList);
        List<SjgCountReportRowVO> rowList = Lists.newArrayList();
        Map<Integer, BigDecimal> curGhQuantityMap = FeignOptional.of(() -> remoteReportWorkFeignApi.getGhQuantity(new TripleDTO<>(start, end, lineIdList)), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        Map<Integer, BigDecimal> preGhQuantityMap = FeignOptional.of(() -> remoteReportWorkFeignApi.getGhQuantity(new TripleDTO<>(preStart, preEnd, lineIdList)), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        //本周合计，上周合计，电量，单耗
        BigDecimal curSum = countSum(currentConsumeMap);
        BigDecimal preSum = countSum(preConsumeMap);
        BigDecimal curGhSum = countSum(curGhQuantityMap);
        BigDecimal preGhSum = countSum(preGhQuantityMap);
        for (SysOfficeVO line : lineList) {
            SjgCountReportRowVO row = new SjgCountReportRowVO();
            row.setCategoryName("电");
            row.setLineId(line.getId());
            row.setLineName(line.getOfficeName());
            row.setCurrentValue(currentConsumeMap.get(line.getId()));
            //单耗：电量 / 该产线钢化产量
            row.setCurrentValueRate(BigDecimalUtil.percent(row.getCurrentValue(), curGhQuantityMap.get(line.getId())));
            row.setPreValue(preConsumeMap.get(line.getId()));
            row.setPreValueRate(BigDecimalUtil.percent(row.getPreValue(), preGhQuantityMap.get(line.getId())));
            row.setValueDiff(BigDecimalUtil.subtract(row.getCurrentValue(), row.getPreValue(), BigDecimalUtil.SCALE_0));
            row.setValueDiffRate(BigDecimalUtil.percent(row.getValueDiff(), row.getCurrentValue()));
            //本周合计，上周合计，电量，单耗
            row.setCurrentValueSum(curSum);
            row.setCurrentValueSumRate(BigDecimalUtil.percent(curSum, curGhSum));
            row.setPreValueSum(preSum);
            row.setPreValueSumRate(BigDecimalUtil.percent(preSum, preGhSum));
            //电量，单耗
            row.setValueDiffSum(BigDecimalUtil.subtract(curSum, preSum, BigDecimalUtil.SCALE_0));
            row.setValueDiffSumRate(BigDecimalUtil.percent(row.getValueDiffSum(), curSum));
            rowList.add(row);
        }
        reportVO.setRowList(rowList);
        return reportVO;
    }

    /**
     * 本周合计
     *
     * @param valueMap 值集合
     * @return
     */
    private BigDecimal countSum(Map<Integer, BigDecimal> valueMap) {
        BigDecimal sum = BigDecimalUtil.sum(valueMap.values());
        return BigDecimalUtil.setScale(sum, BigDecimalUtil.SCALE_0);
    }

    /**
     * 获取时间区间内的电耗量
     *
     * @param start           开始日期
     * @param end             结束日期
     * @param partitionIdList 组织结构节点
     * @return
     */
    private Map<Integer, BigDecimal> eleConsume(LocalDate start, LocalDate end, List<Long> partitionIdList) {
        Map<Integer, BigDecimal> resMap = Maps.newHashMap();
        List<LocalDate> conditionDt = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), start, end);
        List<EpMeterReadAllDataVO> electricAllData = epMeterReadService.classDateAllData(Sets.newHashSet(conditionDt), 1, partitionIdList);
        if(CollUtil.isNotEmpty(electricAllData)){
            electricAllData.forEach(ele -> {
                MapUtil.add(resMap, ele.getSysOfficeId(), ele.getConsume(), (x, y) -> BigDecimalUtil.add(x, y, BigDecimalUtil.SCALE_0));
            });
        }
        return resMap;
    }

    /**
     * 设置深加工统计报表头
     *
     * @param start    开始日期
     * @param end      结束日期
     * @param preStart 前区间开始日期
     * @param preEnd   前区间结束日期
     * @return
     */
    private List<TableHeadColumnDTO> setCountReportHead(LocalDate start, LocalDate end, LocalDate preStart, LocalDate preEnd) {
        String currentStartStr = LocalDateUtil.format(start, "MM.dd");
        String currentEndStr = LocalDateUtil.format(end, "MM.dd");
        String preStartStr = LocalDateUtil.format(preStart, "MM.dd");
        String preEndStr = LocalDateUtil.format(preEnd, "MM.dd");
        List<TableHeadColumnDTO> resList = Lists.newArrayList();
        resList.add(new TableHeadColumnDTO("类别", "categoryName"));
        resList.add(new TableHeadColumnDTO("线别", "lineName"));
        resList.add(new TableHeadColumnDTO("本区间能耗" + currentStartStr + "-" + currentEndStr, "currentValueTuple", Arrays.asList(new TableHeadColumnDTO("电量", "currentValue"), new TableHeadColumnDTO("单耗", "currentValueRate"))));
        resList.add(new TableHeadColumnDTO("上区间能耗，" + preStartStr + "-" + preEndStr, "preValueTuple", Arrays.asList(new TableHeadColumnDTO("电量", "preValue"), new TableHeadColumnDTO("单耗", "preValueRate"))));
        resList.add(new TableHeadColumnDTO("电量增长", "valueDiffSingle", Arrays.asList(new TableHeadColumnDTO("(+/-)", "valueDiff"))));
        resList.add(new TableHeadColumnDTO("电量增长率", "valueDiffRateSingle", Arrays.asList(new TableHeadColumnDTO("(+/-)", "valueDiffRate"))));
        resList.add(new TableHeadColumnDTO("本区间合计", "currentValueSumTuple", Arrays.asList(new TableHeadColumnDTO("电量", "currentValueSum"), new TableHeadColumnDTO("单耗", "currentValueSumRate"))));
        resList.add(new TableHeadColumnDTO("上区间合计", "preValueSumTuple", Arrays.asList(new TableHeadColumnDTO("电量", "preValueSum"), new TableHeadColumnDTO("单耗", "preValueSumRate"))));
        resList.add(new TableHeadColumnDTO("汇总电量增长", "valueDiffSumSingle", Arrays.asList(new TableHeadColumnDTO("(+/-)", "valueDiffSum"))));
        resList.add(new TableHeadColumnDTO("汇总电量增长率", "valueDiffSumRateSingle", Arrays.asList(new TableHeadColumnDTO("(+/-)", "valueDiffSumRate"))));
        return resList;
    }

    /**
     * 深加工单耗-电耗/入库产量
     *
     * @param dto         请求dto
     * @param bit         位数
     * @param granularity 粒度
     * @param dateList    日期区间
     * @return
     */
    private PmsOperateEnergyVO sjgUnitConsume(DpPmsOperateDTO dto, Integer bit, QtManagerConstant.QtGranularityEnum granularity, List<LocalDate> dateList) {
        EpPartition electric = epPartitionService.getOne(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getOfficeId, dto.getOfficeId()).eq(EpPartition::getType, 1));
        Assert.notNull(electric, "能源树上不存在此节点");
        List<LocalDate> conditionDt = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), dto.getStart(), dto.getEnd());
        List<EpMeterReadAllDataVO> electricAllData = epMeterReadService.classDateAllData(Sets.newHashSet(conditionDt), electric.getType(), Arrays.asList(electric.getId()));
        SteccnTableAssertUtil.notEmpty(electricAllData, "全量数据表为空");
        Map<String, BigDecimal> electricValueMap = Maps.newHashMap();
        electricAllData.forEach(ele -> {
            String convert = XAxisUtil.convert(granularity, ele.getReadDate());
            MapUtil.add(electricValueMap, convert, ele.getConsume(), (x, y) -> BigDecimalUtil.add(x, y, bit));
        });
        PmsOperateDriverDTO pmsOperateDriverDTO = new PmsOperateDriverDTO();
        pmsOperateDriverDTO.setStart(dto.getStart());
        pmsOperateDriverDTO.setEnd(dto.getEnd());
        pmsOperateDriverDTO.setGranularity(granularity.getCode());
        pmsOperateDriverDTO.setOfficeId(dto.getOfficeId());
        Map<String, BigDecimal> inStorageMap = FeignOptional.of(() -> remoteCompleteStorageFeignApi.sjgInStorageSum(pmsOperateDriverDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<String> xAisList = Lists.newArrayList();
        List<BigDecimal> valueList = Lists.newArrayList();
        List<BigDecimal> electricValueList = Lists.newArrayList();
        List<BigDecimal> inStorageValueList = Lists.newArrayList();
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate ele = dateList.get(i - 1);
            String convert = XAxisUtil.convert(granularity, ele);
            xAisList.add(convert);
            //深加工单耗：电耗/入库产量
            BigDecimal electricValue = electricValueMap.get(convert);
            String mysqlKey = MysqlDateKeyUtil.convert(granularity, ele);
            BigDecimal inStorageValue = inStorageMap.get(mysqlKey);
            BigDecimal result = BigDecimalUtil.divide(electricValue, inStorageValue);
            valueList.add(result);
            electricValueList.add(electricValue);
            inStorageValueList.add(inStorageValue);
        }
        PmsOperateEnergyVO res = new PmsOperateEnergyVO();
        BigDecimal electricSum = BigDecimalUtil.sum(electricValueList);
        BigDecimal inStorageSum = BigDecimalUtil.sum(inStorageValueList);
        BigDecimal unitConsumeAvg = BigDecimalUtil.divide(electricSum, inStorageSum);
        res.setTotalQuantity(unitConsumeAvg);
        res.setUnitName("kcal/kg");
        CommonBarLineVO commonBarLineVO = new CommonBarLineVO();
        res.setCommonBarLineVO(commonBarLineVO);
        commonBarLineVO.setXAxis(new BaseValueVO(xAisList));
        commonBarLineVO.setLegend(new BaseValueVO(Arrays.asList("单耗(kcal/kg)")));
        commonBarLineVO.setSeries(Arrays.asList(new BaseValueNameVO<>(valueList, "单耗(kcal/kg)")));
        return res;
    }

    /**
     * 基片单耗
     *
     * @param dto         请求dto
     * @param bit         位数
     * @param granularity 粒度
     * @param dateList    日期区间
     * @param wsIdSet     组织结构id集合
     * @return
     */
    private PmsOperateEnergyVO jpUnitConsume(DpPmsOperateDTO dto, Integer bit, QtManagerConstant.QtGranularityEnum granularity, List<LocalDate> dateList,Set<Integer> wsIdSet) {
        //天然气用量*7800+重油用量*1000*9300)/(拉引量*1000)
        EpPartition gas = epPartitionService.getOne(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getOfficeId, dto.getOfficeId()).eq(EpPartition::getType, 3));
        Assert.notNull(gas, "天然气树，不存在此节点");
        EpPartition oil = epPartitionService.getOne(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getOfficeId, dto.getOfficeId()).eq(EpPartition::getType, 4));
        Assert.notNull(oil, "重油树，不存在此节点");
        List<LocalDate> conditionDt = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), dto.getStart(), dto.getEnd());
        //获取全量数据表
        List<EpMeterReadAllDataVO> gasAllData = epMeterReadService.classDateAllData(Sets.newHashSet(conditionDt), gas.getType(), Arrays.asList(gas.getId()));
        List<EpMeterReadAllDataVO> oilAllData = epMeterReadService.classDateAllData(Sets.newHashSet(conditionDt), oil.getType(), Arrays.asList(oil.getId()));
        SteccnTableAssertUtil.notEmpty(gasAllData, "全量数据表为空");
        SteccnTableAssertUtil.notEmpty(oilAllData, "全量数据表为空");
        //获取拉引量
        DpPullForceDTO dpPullForceDTO = new DpPullForceDTO();
        dpPullForceDTO.setStart(dto.getStart());
        dpPullForceDTO.setEnd(dto.getEnd());
        dpPullForceDTO.setGranularity(dto.getGranularity());
        dpPullForceDTO.setWorkshopIdSet(wsIdSet);
        Map<String, BigDecimal> pullForceMap = FeignOptional.of(() -> remoteDpPullForceFeignApi.pullForceSum(dpPullForceDTO), FeignConstant.STECCN_DP_SERVICE_NAME).get();

        Map<String, BigDecimal> gasValueMap = Maps.newHashMap();
        gasAllData.forEach(ele -> {
            String convert = XAxisUtil.convert(granularity, ele.getReadDate());
            MapUtil.add(gasValueMap, convert, ele.getConsume(), (x, y) -> BigDecimalUtil.add(x, y, bit));
        });
        Map<String, BigDecimal> oilValueMap = Maps.newHashMap();
        oilAllData.forEach(ele -> {
            String convert = XAxisUtil.convert(granularity, ele.getReadDate());
            MapUtil.add(oilValueMap, convert, ele.getConsume(), (x, y) -> BigDecimalUtil.add(x, y, bit));
        });
        List<String> xAisList = Lists.newArrayList();
        List<BigDecimal> valueList = Lists.newArrayList();
        List<BigDecimal> gasValueList = Lists.newArrayList();
        List<BigDecimal> oilValueList = Lists.newArrayList();
        List<BigDecimal> pullForceValueList = Lists.newArrayList();
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate ele = dateList.get(i - 1);
            String convert = XAxisUtil.convert(granularity, ele);
            xAisList.add(convert);
            //y轴：（天然气用量*7800+重油用量*1000*9300)/(拉引量*1000)
            BigDecimal gasValue = gasValueMap.get(convert);
            BigDecimal oilValue = oilValueMap.get(convert);
            String mysqlKey = MysqlDateKeyUtil.convert(granularity, ele);
            BigDecimal pullForceValue = pullForceMap.get(mysqlKey);
            BigDecimal result = UnitConsumeUtil.unitConsume(gasValue, oilValue, pullForceValue, bit);
            valueList.add(result);
            gasValueList.add(gasValue);
            oilValueList.add(oilValue);
            pullForceValueList.add(pullForceValue);
        }
        PmsOperateEnergyVO res = new PmsOperateEnergyVO();
        BigDecimal gasSum = BigDecimalUtil.sum(gasValueList);
        BigDecimal oilSum = BigDecimalUtil.sum(oilValueList);
        BigDecimal pullForceSum = BigDecimalUtil.sum(pullForceValueList);
        BigDecimal unitConsumeAvg = UnitConsumeUtil.unitConsume(gasSum, oilSum, pullForceSum, bit);
        res.setTotalQuantity(unitConsumeAvg);
        res.setUnitName("kcal/kg");
        CommonBarLineVO commonBarLineVO = new CommonBarLineVO();
        res.setCommonBarLineVO(commonBarLineVO);
        commonBarLineVO.setXAxis(new BaseValueVO(xAisList));
        commonBarLineVO.setLegend(new BaseValueVO(Arrays.asList("单耗(kcal/kg)")));
        commonBarLineVO.setSeries(Arrays.asList(new BaseValueNameVO<>(valueList, "单耗(kcal/kg)")));
        return res;
    }

    /**
     * 能源值
     *
     * @param dto        dto
     * @param energyType 能源类型
     * @param bit        单位
     * @return
     */
    private PmsOperateEnergyVO energy(DpPmsOperateDTO dto, Integer energyType, Integer bit) {
        EpEnergyType energy = epEnergyTypeService.getById(energyType);
        Assert.notNull(energy, "能源类型不存在");
        Map<Long, String> measureUnitMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(Sets.newHashSet(energy.getMeasureUnit())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularity, "粒度不存在");
        List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
        Assert.notEmpty(dateList, "日期区间有误");
        List<LocalDate> conditionDt = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), dto.getStart(), dto.getEnd());
        //根据能源类型 + 组织机构id获取
        EpPartition one = epPartitionService.getOne(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getOfficeId, dto.getOfficeId()).eq(EpPartition::getType, energy.getId()));
        Assert.notNull(one, "能源树，不存在此节点");
        //获取全量数据表
        List<EpMeterReadAllDataVO> classDateAllData = epMeterReadService.classDateAllData(Sets.newHashSet(conditionDt), energyType, Arrays.asList(one.getId()));
        SteccnTableAssertUtil.notEmpty(classDateAllData, "全量数据表为空");
        //构建能源树
        Map<String, BigDecimal> valueMap = Maps.newHashMap();
        classDateAllData.forEach(ele -> {
            String convert = XAxisUtil.convert(granularity, ele.getReadDate());
            MapUtil.add(valueMap, convert, ele.getConsume(), (x, y) -> BigDecimalUtil.add(x, y, bit));
        });
        List<String> xAisList = Lists.newArrayList();
        List<BigDecimal> valueList = Lists.newArrayList();
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate ele = dateList.get(i - 1);
            String convert = XAxisUtil.convert(granularity, ele);
            xAisList.add(convert);
            valueList.add(BigDecimalUtil.setScale(valueMap.get(convert), bit));
        }
        PmsOperateEnergyVO res = new PmsOperateEnergyVO();
        res.setTotalQuantity(BigDecimalUtil.setScale(BigDecimalUtil.sum(valueMap.values()), bit));
        res.setUnitName(measureUnitMap.getOrDefault(energy.getMeasureUnit(), Strings.EMPTY));
        CommonBarLineVO commonBarLineVO = new CommonBarLineVO();
        res.setCommonBarLineVO(commonBarLineVO);
        commonBarLineVO.setXAxis(new BaseValueVO(xAisList));
        commonBarLineVO.setLegend(new BaseValueVO(Arrays.asList(energy.getName() + "(" + res.getUnitName() + ")")));
        commonBarLineVO.setSeries(Arrays.asList(new BaseValueNameVO<>(valueList, energy.getName() + "(" + res.getUnitName() + ")")));
        return res;
    }
}
