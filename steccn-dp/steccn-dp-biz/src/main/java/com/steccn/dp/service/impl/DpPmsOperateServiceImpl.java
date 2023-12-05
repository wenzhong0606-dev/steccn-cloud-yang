package com.steccn.dp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.MapUtil;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.common.util.UnitLengthUtil;
import com.steccn.common.vo.BaseValueNameVO;
import com.steccn.common.vo.BaseValueVO;
import com.steccn.common.vo.CommonBarLineVO;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.dp.mapper.AdsPmsCompleteStorageMapper;
import com.steccn.dp.mapper.AdsPmsPullForceMapper;
import com.steccn.dp.mapper.AdsPmsReportWorkMapper;
import com.steccn.dp.service.DpPmsOperateService;
import com.steccn.dp.vo.DpPmsOperateCompleteStorageVO;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareUnitConvertFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.util.UnitConvertUtil;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.XAxisUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 生产管理-运营驾驶舱服务类
 * @date 2023/11/29
 */
@Service
@RequiredArgsConstructor
public class DpPmsOperateServiceImpl implements DpPmsOperateService {

    private final AdsPmsCompleteStorageMapper adsPmsCompleteStorageMapper;
    private final AdsPmsReportWorkMapper adsPmsReportWorkMapper;
    private final AdsPmsPullForceMapper adsPmsPullForceMapper;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final RemoteSpareUnitConvertFeignApi remoteSpareUnitConvertFeignApi;
    private final RemoteDictFeignApi remoteDictFeignApi;

    @Override
    public DpPmsOperateCompleteStorageVO inStorage(DpPmsOperateDTO dto) {
        SysOfficeParentDTO sysOfficeParentDTO = new SysOfficeParentDTO();
        sysOfficeParentDTO.setId(dto.getOfficeId());
        sysOfficeParentDTO.setOfficeType(SysOfficeConstant.WORKSHOP);
        List<SysOfficeVO> sysOfficeVOList = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(sysOfficeParentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(sysOfficeVOList, "不存在车间节点");
        long sjgCount = sysOfficeVOList.stream().filter(ele -> ele.getOfficeName().indexOf("深加工") != -1).count();
        DpPmsOperateCompleteStorageVO res;
        if (sjgCount != 0) {
            res = sjgInStorage(dto, sysOfficeVOList, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH);
        } else {
            res = jpInStorage(dto, sysOfficeVOList, UnitLengthUtil.UNIT_TON_LENGTH);
        }
        return res;
    }

    /**
     * 深加工入库
     * 入库产量：柱状图，对应时间所有深加工车间的完工入库入库明细（入库单不为冲销和删除状态）数量汇总，转化为平方米，保留两位小数
     * 实际使用数：柱状图，取自磨边工位报工加工数，转换为平方米
     * 入库合格率：折线图，入库产量 / 实际使用数 * 100，保留两位小数，百分比显示
     *
     * @param dto
     * @param sysOfficeVOList
     * @return
     */
    private DpPmsOperateCompleteStorageVO sjgInStorage(DpPmsOperateDTO dto, List<SysOfficeVO> sysOfficeVOList, Integer bit) {
        //获取车间主键集合
        Set<Integer> wsIdSet = sysOfficeVOList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        List<TripleDTO<String, Long, BigDecimal>> storageList;
        List<TripleDTO<String, Long, BigDecimal>> actualUseList;
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            storageList = adsPmsCompleteStorageMapper.inStorageMaterialDay(dto.getStart(), dto.getEnd(), wsIdSet);
            actualUseList = adsPmsReportWorkMapper.actualUseMaterialDay(dto.getStart(), dto.getEnd(), wsIdSet);
        } else {
            storageList = adsPmsCompleteStorageMapper.inStorageMaterialMonth(dto.getStart(), dto.getEnd(), wsIdSet);
            actualUseList = adsPmsReportWorkMapper.actualUseMaterialMonth(dto.getStart(), dto.getEnd(), wsIdSet);
        }
        SysDictVO pieceSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO smSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(pieceSysDict, "片单位不存在");
        Assert.notNull(smSysDict, "平方米单位不存在");
        Map<String, BigDecimal> storageMap = unitConvert(pieceSysDict, smSysDict, storageList, bit);
        Map<String, BigDecimal> actualUseMap = unitConvert(pieceSysDict, smSysDict, actualUseList, bit);

        DpPmsOperateCompleteStorageVO resVo = new DpPmsOperateCompleteStorageVO();
        resVo.setTotalQuantity(BigDecimalUtil.setScale(BigDecimalUtil.sum(storageMap.values()), bit));
        resVo.setTotalRate(BigDecimalUtil.percent(resVo.getTotalQuantity(), BigDecimalUtil.sum(actualUseMap.values())));
        //柱状图 + 折线图
        CommonBarLineVO commonBarLineVO = new CommonBarLineVO();
        //根据粒度 分割日期
        List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
        List<String> xAisList = Lists.newArrayList();
        List<BigDecimal> inStorageQuantityList = Lists.newArrayList();
        List<BigDecimal> rateList = Lists.newArrayList();
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate ele = dateList.get(i - 1);
            xAisList.add(XAxisUtil.convert(granularity, ele));
            //获取值的方式必须统一
            String key;
            if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
                key = ele.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else {
                key = ele.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            }
            BigDecimal storage = storageMap.get(key);
            BigDecimal actualUse = actualUseMap.get(key);
            inStorageQuantityList.add(storage);
            rateList.add(BigDecimalUtil.percent(storage, actualUse));
        }
        List<String> legendList = Arrays.asList("入库产量（㎡）", "入库成品率");
        commonBarLineVO.setXAxis(new BaseValueVO(xAisList));
        commonBarLineVO.setLegend(new BaseValueVO<>(legendList));
        BaseValueNameVO<List<BigDecimal>> storageBv = new BaseValueNameVO<>(inStorageQuantityList, "入库产量（㎡）");
        BaseValueNameVO<List<BigDecimal>> pullForceBv = new BaseValueNameVO<>(rateList, "入库成品率");
        commonBarLineVO.setSeries(Arrays.asList(storageBv, pullForceBv));
        resVo.setCommonBarLineVO(commonBarLineVO);
        return resVo;
    }

    /**
     * 基片车间入库
     * 入库产量：柱状图，对应时间所有基片车间的完工入库入库明细（入库单不为冲销和删除状态）数量汇总，转化为重量，保留两位小数，单位吨
     * 入库合格率：折线图，入库产量 / 拉引量 * 100，保留两位小数，百分比显示
     *
     * @param dto             请求参数
     * @param sysOfficeVOList 组织机构集合
     * @param bit             保留几位
     * @return
     */
    private DpPmsOperateCompleteStorageVO jpInStorage(DpPmsOperateDTO dto, List<SysOfficeVO> sysOfficeVOList, int bit) {
        Set<Integer> wsIdSet = sysOfficeVOList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        //获取粒度
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        List<TripleDTO<String, Long, BigDecimal>> storageList;
        List<TupleDTO<String, BigDecimal>> pullForceList;
        //根据车间+时间查询入库数
        //根据车间+时间查询拉引量
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            storageList = adsPmsCompleteStorageMapper.inStorageMaterialDay(dto.getStart(), dto.getEnd(), wsIdSet);
            pullForceList = adsPmsPullForceMapper.pullForceDay(dto.getStart(), dto.getEnd(), wsIdSet);
        } else {
            storageList = adsPmsCompleteStorageMapper.inStorageMaterialMonth(dto.getStart(), dto.getEnd(), wsIdSet);
            pullForceList = adsPmsPullForceMapper.pullForceMonth(dto.getStart(), dto.getEnd(), wsIdSet);
        }
        //获取总数
        BigDecimal totalInStorageQuantity = null;
        BigDecimal totalPullForceQuantity = null;
        Map<String, BigDecimal> storageMap = Maps.newHashMap();
        Map<String, BigDecimal> pullForceMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(storageList)) {
            //进行单位转换，放入map
            SysDictVO pieceSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO tonSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_TON, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(pieceSysDict, "片单位不存在");
            Assert.notNull(tonSysDict, "吨单位不存在");
            storageMap = unitConvert(pieceSysDict, tonSysDict, storageList, bit);
            totalInStorageQuantity = BigDecimalUtil.sum(storageMap.values());
        }
        if (CollUtil.isNotEmpty(pullForceList)) {
            pullForceList.forEach(ele -> pullForceMap.put(ele.getField1(), ele.getField2()));
            totalPullForceQuantity = BigDecimalUtil.sum(pullForceMap.values());
        }

        DpPmsOperateCompleteStorageVO resVo = new DpPmsOperateCompleteStorageVO();
        resVo.setTotalQuantity(totalInStorageQuantity);
        resVo.setTotalRate(BigDecimalUtil.percent(totalInStorageQuantity, totalPullForceQuantity));
        //柱状图 + 折线图
        CommonBarLineVO commonBarLineVO = new CommonBarLineVO();
        //根据粒度 分割日期
        List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
        List<String> xAisList = Lists.newArrayList();
        List<BigDecimal> inStorageQuantityList = Lists.newArrayList();
        List<BigDecimal> rateList = Lists.newArrayList();
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate ele = dateList.get(i - 1);
            xAisList.add(XAxisUtil.convert(granularity, ele));
            //获取值的方式必须统一
            String key;
            if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
                key = ele.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else {
                key = ele.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            }
            BigDecimal storage = storageMap.get(key);
            BigDecimal pullForce = pullForceMap.get(key);
            inStorageQuantityList.add(storage);
            rateList.add(BigDecimalUtil.percent(storage, pullForce));
        }
        List<String> legendList = Arrays.asList("入库产量（T）", "入库成品率");
        commonBarLineVO.setXAxis(new BaseValueVO(xAisList));
        commonBarLineVO.setLegend(new BaseValueVO<>(legendList));
        BaseValueNameVO<List<BigDecimal>> storageBv = new BaseValueNameVO<>(inStorageQuantityList, "入库产量（T）");
        BaseValueNameVO<List<BigDecimal>> pullForceBv = new BaseValueNameVO<>(rateList, "入库成品率");
        commonBarLineVO.setSeries(Arrays.asList(storageBv, pullForceBv));
        resVo.setCommonBarLineVO(commonBarLineVO);
        return resVo;
    }

    /**
     * 单位转换
     *
     * @param sourceSysDict 原始单位
     * @param targetSysDict 目标单位
     * @param dataList      数据集合
     * @return map
     */
    private Map<String, BigDecimal> unitConvert(SysDictVO sourceSysDict, SysDictVO targetSysDict, List<TripleDTO<String, Long, BigDecimal>> dataList, Integer bit) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(dataList)) {
            Set<Long> materialIdSet = dataList.stream().map(TripleDTO::getField2).collect(Collectors.toSet());
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            dataList.forEach(ele -> {
                List<SysSpareUnitConversionVO> convertList = materialConvertMap.get(ele.getField2());
                BigDecimal convertQuantity = UnitConvertUtil.convertMaterial(convertList, sourceSysDict.getId(), targetSysDict.getId(), ele.getField3(), bit);
                MapUtil.add(resMap, ele.getField1(), convertQuantity, (x, y) -> BigDecimalUtil.add(x, y, bit));
            });
        }
        return resMap;
    }


    @Override
    public DpPmsOperateCompleteStorageVO complete(DpPmsOperateDTO dto) {
        SysOfficeParentDTO sysOfficeParentDTO = new SysOfficeParentDTO();
        sysOfficeParentDTO.setId(dto.getOfficeId());
        sysOfficeParentDTO.setOfficeType(SysOfficeConstant.WORKSHOP);
        List<SysOfficeVO> sysOfficeVOList = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(sysOfficeParentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(sysOfficeVOList, "不存在车间节点");
        //判断是基片还是深加工
        long sjgCount = sysOfficeVOList.stream().filter(ele -> ele.getOfficeName().indexOf("深加工") != -1).count();
        DpPmsOperateCompleteStorageVO res;
        if (sjgCount != 0) {
            res = sjgComplete(dto, sysOfficeVOList, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH);
        } else {
            res = jpComplete(dto, sysOfficeVOList, UnitLengthUtil.UNIT_TON_LENGTH);
        }
        return res;
    }

    /**
     * 基片加工产量
     *
     * @param dto             dto
     * @param sysOfficeVOList 组织机构集合
     * @param bit             保留位数
     * @return
     */
    private DpPmsOperateCompleteStorageVO jpComplete(DpPmsOperateDTO dto, List<SysOfficeVO> sysOfficeVOList, int bit) {
        Set<Integer> wsIdSet = sysOfficeVOList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        //获取粒度
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularity, "粒度不存在");
        List<TripleDTO<String, Long, BigDecimal>> completeList;
        List<TupleDTO<String, BigDecimal>> pullForceList;
        //根据车间+时间查询入库数
        //根据车间+时间查询拉引量
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            completeList = adsPmsReportWorkMapper.jpCompleteMaterialDay(dto.getStart(), dto.getEnd(), wsIdSet);
            pullForceList = adsPmsPullForceMapper.pullForceDay(dto.getStart(), dto.getEnd(), wsIdSet);
        } else {
            completeList = adsPmsReportWorkMapper.jpCompleteMaterialMonth(dto.getStart(), dto.getEnd(), wsIdSet);
            pullForceList = adsPmsPullForceMapper.pullForceMonth(dto.getStart(), dto.getEnd(), wsIdSet);
        }
        //获取总数
        BigDecimal totalQuantity = null;
        BigDecimal totalPullForceQuantity = null;
        Map<String, BigDecimal> completeMap = Maps.newHashMap();
        Map<String, BigDecimal> pullForceMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(completeList)) {
            //进行单位转换，放入map
            Set<Long> materialIdSet = completeList.stream().map(TripleDTO::getField2).collect(Collectors.toSet());
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO pieceSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO tonSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_TON, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(pieceSysDict, "片单位不存在");
            Assert.notNull(tonSysDict, "吨单位不存在");
            completeList.forEach(ele -> {
                List<SysSpareUnitConversionVO> convertList = materialConvertMap.get(ele.getField2());
                BigDecimal tonQuantity = UnitConvertUtil.convertMaterial(convertList, pieceSysDict.getId(), tonSysDict.getId(), ele.getField3(), bit);
                completeMap.put(ele.getField1(), BigDecimalUtil.add(completeMap.get(ele.getField1()), tonQuantity, bit));
            });
            totalQuantity = BigDecimalUtil.sum(completeMap.values());
        }
        if (CollUtil.isNotEmpty(pullForceList)) {
            pullForceList.forEach(ele -> pullForceMap.put(ele.getField1(), ele.getField2()));
            totalPullForceQuantity = BigDecimalUtil.sum(pullForceMap.values());
        }
        DpPmsOperateCompleteStorageVO resVo = new DpPmsOperateCompleteStorageVO();
        resVo.setTotalQuantity(totalQuantity);
        resVo.setTotalRate(BigDecimalUtil.percent(totalQuantity, totalPullForceQuantity));
        //柱状图 + 折线图
        CommonBarLineVO commonBarLineVO = new CommonBarLineVO();
        //根据粒度 分割日期
        List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
        List<String> xAisList = Lists.newArrayList();
        List<BigDecimal> completeQuantityList = Lists.newArrayList();
        List<BigDecimal> rateList = Lists.newArrayList();
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate ele = dateList.get(i - 1);
            xAisList.add(XAxisUtil.convert(granularity, ele));
            //获取值的方式必须统一
            String key;
            if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
                key = ele.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else {
                key = ele.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            }
            BigDecimal complete = completeMap.get(key);
            BigDecimal pullForce = pullForceMap.get(key);
            completeQuantityList.add(complete);
            rateList.add(BigDecimalUtil.percent(complete, pullForce));
        }
        List<String> legendList = Arrays.asList("生产产量（T）", "生产成品率");
        commonBarLineVO.setXAxis(new BaseValueVO(xAisList));
        commonBarLineVO.setLegend(new BaseValueVO<>(legendList));
        BaseValueNameVO<List<BigDecimal>> completeBv = new BaseValueNameVO<>(completeQuantityList, "生产产量（T）");
        BaseValueNameVO<List<BigDecimal>> pullForceBv = new BaseValueNameVO<>(rateList, "生产成品率");
        commonBarLineVO.setSeries(Arrays.asList(completeBv, pullForceBv));
        resVo.setCommonBarLineVO(commonBarLineVO);
        return resVo;
    }

    /**
     * 深加工产量
     * 入库产量：柱状图，对应时间所有深加工车间的完工入库入库明细（入库单不为冲销和删除状态）数量汇总，转化为平方米，保留两位小数
     * 入库合格率：折线图，入库产量 / 实际使用数 * 100，保留两位小数，百分比显示
     *
     * @param dto             dto
     * @param sysOfficeVOList 组织机构集合
     * @param bit             保留位数
     * @return
     */
    private DpPmsOperateCompleteStorageVO sjgComplete(DpPmsOperateDTO dto, List<SysOfficeVO> sysOfficeVOList, int bit) {
        Set<Integer> wsIdSet = sysOfficeVOList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        //获取粒度
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularity, "粒度不存在");
        List<TripleDTO<String, Long, BigDecimal>> completeList;
        List<TripleDTO<String, Long, BigDecimal>> actualUseList;
        //根据车间+时间查询完工数
        //根据车间+时间查询实际使用量
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            completeList = adsPmsReportWorkMapper.sjgCompleteMaterialDay(dto.getStart(), dto.getEnd(), wsIdSet);
            actualUseList = adsPmsReportWorkMapper.actualUseMaterialDay(dto.getStart(), dto.getEnd(), wsIdSet);
        } else {
            completeList = adsPmsReportWorkMapper.sjgCompleteMaterialMonth(dto.getStart(), dto.getEnd(), wsIdSet);
            actualUseList = adsPmsReportWorkMapper.actualUseMaterialMonth(dto.getStart(), dto.getEnd(), wsIdSet);
        }
        //获取总数
        BigDecimal totalQuantity = null;
        BigDecimal totalActualUseQuantity = null;
        Map<String, BigDecimal> completeMap = Maps.newHashMap();
        Map<String, BigDecimal> actualUseMap = Maps.newHashMap();
        SysDictVO pieceSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sqSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(pieceSysDict, "片单位不存在");
        Assert.notNull(sqSysDict, "平方米单位不存在");
        if (CollUtil.isNotEmpty(completeList)) {
            //进行单位转换，放入map
            completeMap = unitConvert(pieceSysDict, sqSysDict, completeList, bit);
            totalQuantity = BigDecimalUtil.sum(completeMap.values());
        }
        if (CollUtil.isNotEmpty(actualUseList)) {
            actualUseMap = unitConvert(pieceSysDict, sqSysDict, actualUseList, bit);
            totalActualUseQuantity = BigDecimalUtil.sum(actualUseMap.values());
        }
        DpPmsOperateCompleteStorageVO resVo = new DpPmsOperateCompleteStorageVO();
        resVo.setTotalQuantity(totalQuantity);
        resVo.setTotalRate(BigDecimalUtil.percent(totalQuantity, totalActualUseQuantity));
        //柱状图 + 折线图
        CommonBarLineVO commonBarLineVO = new CommonBarLineVO();
        //根据粒度 分割日期
        List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
        List<String> xAisList = Lists.newArrayList();
        List<BigDecimal> completeQuantityList = Lists.newArrayList();
        List<BigDecimal> rateList = Lists.newArrayList();
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate ele = dateList.get(i - 1);
            xAisList.add(XAxisUtil.convert(granularity, ele));
            //获取值的方式必须统一
            String key;
            if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
                key = ele.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else {
                key = ele.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            }
            BigDecimal complete = completeMap.get(key);
            BigDecimal actualUse = actualUseMap.get(key);
            completeQuantityList.add(complete);
            rateList.add(BigDecimalUtil.percent(complete, actualUse));
        }
        List<String> legendList = Arrays.asList("生产产量（㎡）", "生产成品率");
        commonBarLineVO.setXAxis(new BaseValueVO(xAisList));
        commonBarLineVO.setLegend(new BaseValueVO<>(legendList));
        BaseValueNameVO<List<BigDecimal>> completeBv = new BaseValueNameVO<>(completeQuantityList, "生产产量（㎡）");
        BaseValueNameVO<List<BigDecimal>> pullForceBv = new BaseValueNameVO<>(rateList, "生产成品率");
        commonBarLineVO.setSeries(Arrays.asList(completeBv, pullForceBv));
        resVo.setCommonBarLineVO(commonBarLineVO);
        return resVo;
    }

}
