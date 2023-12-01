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
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.common.util.UnitLengthUtil;
import com.steccn.common.vo.BaseValueNameVO;
import com.steccn.common.vo.BaseValueVO;
import com.steccn.common.vo.CommonBarLineVO;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.dp.mapper.AdsPmsCompleteStorageMapper;
import com.steccn.dp.mapper.AdsPmsPullForceMapper;
import com.steccn.dp.mapper.AdsPmsReportWorkMapper;
import com.steccn.dp.service.AdsPmsReportWorkService;
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
        Set<Integer> wsIdSet = sysOfficeVOList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        //获取粒度
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularity, "粒度不存在");
        List<TripleDTO<String, Long, BigDecimal>> storageList = null;
        List<TupleDTO<String, BigDecimal>> pullForceList = null;
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
            Set<Long> materialIdSet = storageList.stream().map(TripleDTO::getField2).collect(Collectors.toSet());
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO pieceSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO tonSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_TON, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(pieceSysDict, "片单位不存在");
            Assert.notNull(tonSysDict, "吨单位不存在");
            storageList.forEach(ele -> {
                List<SysSpareUnitConversionVO> convertList = materialConvertMap.get(ele.getField2());
                BigDecimal tonQuantity = UnitConvertUtil.convertMaterial(convertList, pieceSysDict.getId(), tonSysDict.getId(), ele.getField3(), UnitLengthUtil.UNIT_TON_LENGTH);
                storageMap.put(ele.getField1(), BigDecimalUtil.add(storageMap.get(ele.getField1()), tonQuantity, UnitLengthUtil.UNIT_TON_LENGTH));
            });
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

    @Override
    public DpPmsOperateCompleteStorageVO complete(DpPmsOperateDTO dto) {
        SysOfficeParentDTO sysOfficeParentDTO = new SysOfficeParentDTO();
        sysOfficeParentDTO.setId(dto.getOfficeId());
        sysOfficeParentDTO.setOfficeType(SysOfficeConstant.WORKSHOP);
        List<SysOfficeVO> sysOfficeVOList = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(sysOfficeParentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(sysOfficeVOList, "不存在车间节点");
        //判断是基片还是深加工
        long type = sysOfficeVOList.stream().filter(ele -> ele.getOfficeName().indexOf("基片") != -1).count();

        Set<Integer> wsIdSet = sysOfficeVOList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        //获取粒度
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularity, "粒度不存在");
        List<TripleDTO<String, Long, BigDecimal>> completeList;
        List<TupleDTO<String, BigDecimal>> pullForceList;
        //根据车间+时间查询入库数
        //根据车间+时间查询拉引量
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            if (type == 0) {
                completeList = adsPmsReportWorkMapper.sjgCompleteMaterialDay(dto.getStart(), dto.getEnd(), wsIdSet);
            } else {
                completeList = adsPmsReportWorkMapper.jpCompleteMaterialDay(dto.getStart(), dto.getEnd(), wsIdSet);
            }
            pullForceList = adsPmsPullForceMapper.pullForceDay(dto.getStart(), dto.getEnd(), wsIdSet);
        } else {
            if (type == 0) {
                completeList = adsPmsReportWorkMapper.sjgCompleteMaterialMonth(dto.getStart(), dto.getEnd(), wsIdSet);
            } else {
                completeList = adsPmsReportWorkMapper.jpCompleteMaterialMonth(dto.getStart(), dto.getEnd(), wsIdSet);
            }
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
                BigDecimal tonQuantity = UnitConvertUtil.convertMaterial(convertList, pieceSysDict.getId(), tonSysDict.getId(), ele.getField3(), UnitLengthUtil.UNIT_TON_LENGTH);
                completeMap.put(ele.getField1(), BigDecimalUtil.add(completeMap.get(ele.getField1()), tonQuantity, UnitLengthUtil.UNIT_TON_LENGTH));
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

}
