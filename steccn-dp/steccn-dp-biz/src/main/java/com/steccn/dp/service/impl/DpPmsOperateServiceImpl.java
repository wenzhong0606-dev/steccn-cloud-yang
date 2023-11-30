package com.steccn.dp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.common.util.UnitLengthUtil;
import com.steccn.common.vo.BaseValueNameVO;
import com.steccn.common.vo.BaseValueVO;
import com.steccn.common.vo.CommonBarLineVO;
import com.steccn.dp.domain.AdsPmsCompleteStorage;
import com.steccn.dp.domain.AdsPmsPullForce;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.dp.mapper.AdsPmsCompleteStorageMapper;
import com.steccn.dp.mapper.AdsPmsPullForceMapper;
import com.steccn.dp.service.DpPmsOperateService;
import com.steccn.dp.vo.DpPmsOperateInStorageVO;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
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
import java.util.*;
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
    private final AdsPmsPullForceMapper adsPmsPullForceMapper;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final RemoteSpareUnitConvertFeignApi remoteSpareUnitConvertFeignApi;
    private final RemoteDictFeignApi remoteDictFeignApi;

    @Override
    public DpPmsOperateInStorageVO inStorage(DpPmsOperateDTO dto) {
        SysOfficeParentDTO sysOfficeParentDTO = new SysOfficeParentDTO();
        sysOfficeParentDTO.setId(dto.getOfficeId());
        sysOfficeParentDTO.setOfficeType(SysOfficeConstant.WORKSHOP);
        List<SysOfficeVO> sysOfficeVOList = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(sysOfficeParentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(sysOfficeVOList, "不存在车间节点");
        Set<Integer> wsIdSet = sysOfficeVOList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        //根据车间+时间查询入库数
        List<AdsPmsCompleteStorage> storageList = adsPmsCompleteStorageMapper.inStorageMaterialDay(dto.getStart(), dto.getEnd(), wsIdSet);
        //根据车间+时间查询拉引量
        List<AdsPmsPullForce> pullForceList = adsPmsPullForceMapper.pullForceDay(dto.getStart(), dto.getEnd(), wsIdSet);
        //获取总数
        BigDecimal totalInStorageQuantity = null;
        BigDecimal totalPullForceQuantity = null;
        Map<LocalDate, BigDecimal> storageMap = Maps.newHashMap();
        Map<LocalDate, BigDecimal> pullForceMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(storageList)) {
            //进行单位转换，放入map
            Set<Long> materialIdSet = storageList.stream().map(AdsPmsCompleteStorage::getMaterialId).collect(Collectors.toSet());
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO pieceSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO tonSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_TON, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(pieceSysDict, "片单位不存在");
            Assert.notNull(tonSysDict, "吨单位不存在");
            storageList.forEach(ele -> {
                List<SysSpareUnitConversionVO> convertList = materialConvertMap.get(ele.getMaterialId());
                BigDecimal tonQuantity = UnitConvertUtil.convertMaterial(convertList, pieceSysDict.getId(), tonSysDict.getId(), ele.getQuantity1d(), UnitLengthUtil.UNIT_TON_LENGTH);
                storageMap.put(ele.getDt(), BigDecimalUtil.add(storageMap.get(ele.getDt()), tonQuantity, UnitLengthUtil.UNIT_TON_LENGTH));
            });
            totalInStorageQuantity = BigDecimalUtil.sum(storageMap.values());
        }
        if (CollUtil.isNotEmpty(pullForceList)) {
            List<BigDecimal> pullForceQuantityList = pullForceList.stream().map(AdsPmsPullForce::getQuantity).collect(Collectors.toList());
            totalPullForceQuantity = BigDecimalUtil.sum(pullForceQuantityList);
            pullForceList.forEach(ele -> pullForceMap.put(ele.getDt(), ele.getQuantity()));
        }

        DpPmsOperateInStorageVO resVo = new DpPmsOperateInStorageVO();
        resVo.setTotalInStorageQuantity(totalInStorageQuantity);
        resVo.setTotalInStorageRate(BigDecimalUtil.percent(totalInStorageQuantity, totalPullForceQuantity));
        //柱状图 + 折线图
        CommonBarLineVO commonBarLineVO = new CommonBarLineVO();
        //根据粒度 分割日期
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularity, "粒度不存在");
        List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
        List<String> xAisList = Lists.newArrayList();
        List<BigDecimal> inStorageQuantityList = Lists.newArrayList();
        List<BigDecimal> rateList = Lists.newArrayList();
        dateList.forEach(ele -> {
            xAisList.add(XAxisUtil.convert(granularity, ele));
            BigDecimal storage = storageMap.get(ele);
            BigDecimal pullForce = pullForceMap.get(ele);
            inStorageQuantityList.add(storage);
            rateList.add(BigDecimalUtil.percent(storage, pullForce));
        });
        List<String> legendList = Arrays.asList("入库产量（T）", "入库成品率");
        commonBarLineVO.setXAxis(new BaseValueVO(xAisList));
        commonBarLineVO.setLegend(new BaseValueVO<>(legendList));
        BaseValueNameVO<List<BigDecimal>> storageBv = new BaseValueNameVO<>(inStorageQuantityList, "入库产量（T）");
        BaseValueNameVO<List<BigDecimal>> pullForceBv = new BaseValueNameVO<>(rateList, "入库成品率");
        commonBarLineVO.setSeries(Arrays.asList(storageBv, pullForceBv));
        resVo.setCommonBarLineVO(commonBarLineVO);
        return resVo;
    }
}
