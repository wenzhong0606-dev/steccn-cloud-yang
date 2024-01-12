package com.steccn.pms.service.impl;

import cn.hutool.core.lang.Assert;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.common.util.UnitLengthUtil;
import com.steccn.common.vo.BaseValueNameVO;
import com.steccn.common.vo.BaseValueVO;
import com.steccn.common.vo.CommonBarLineVO;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.PmsOperateDriverDTO;
import com.steccn.pms.dto.PullForceSumDTO;
import com.steccn.pms.service.*;
import com.steccn.pms.vo.PmsOperateDriverCompleteStorageVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.MysqlDateKeyUtil;
import com.steccn.qms.util.XAxisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 运营驾驶舱-完工数据和入库数据
 * @createDate 2024-01-11 15:18:51
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PmsOperateDriverServiceImpl implements PmsOperateDriverService {

    private final RemoteOfficeFeign remoteOfficeFeign;
    private final PmsPullForceService pmsPullForceService;
    private final PmsCompleteStorageService pmsCompleteStorageService;
    private final PmsOfficeManager pmsOfficeManager;
    private final PmsReportWorkService pmsReportWorkService;


    @Override
    public PmsOperateDriverCompleteStorageVO inStorage(PmsOperateDriverDTO dto) {
        SysOfficeParentDTO sysOfficeParentDTO = new SysOfficeParentDTO();
        sysOfficeParentDTO.setId(dto.getOfficeId());
        sysOfficeParentDTO.setOfficeType(SysOfficeConstant.WORKSHOP);
        List<SysOfficeVO> sysOfficeVOList = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(sysOfficeParentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(sysOfficeVOList, "不存在车间节点");
        long sjgCount = sysOfficeVOList.stream().filter(ele -> ele.getOfficeName().indexOf("深加工") != -1).count();
        Set<Integer> wsIdSet = sysOfficeVOList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        //获取车间下的产线
        List<SysOfficeVO> lineList = pmsOfficeManager.getChildren(wsIdSet, SysOfficeConstant.PRODUCT_LINE);
        Assert.notEmpty(lineList, "车间下暂无产线");
        Set<Integer> lineIdSet = lineList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());

        PmsOperateDriverCompleteStorageVO res;
        if (sjgCount != 0) {
            res = sjgInStorage(dto, lineIdSet, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH);
        } else {
            res = jpInStorage(dto, lineIdSet, UnitLengthUtil.UNIT_TON_LENGTH);
        }
        return res;
    }

    @Override
    public PmsOperateDriverCompleteStorageVO complete(PmsOperateDriverDTO dto) {
        SysOfficeParentDTO sysOfficeParentDTO = new SysOfficeParentDTO();
        sysOfficeParentDTO.setId(dto.getOfficeId());
        sysOfficeParentDTO.setOfficeType(SysOfficeConstant.WORKSHOP);
        List<SysOfficeVO> sysOfficeVOList = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(sysOfficeParentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notEmpty(sysOfficeVOList, "不存在车间节点");
        //判断是基片还是深加工
        long sjgCount = sysOfficeVOList.stream().filter(ele -> ele.getOfficeName().indexOf("深加工") != -1).count();
        //获取产线id
        Set<Integer> wsIdSet = sysOfficeVOList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        //获取车间下的产线
        List<SysOfficeVO> lineList = pmsOfficeManager.getChildren(wsIdSet, SysOfficeConstant.PRODUCT_LINE);
        Assert.notEmpty(lineList, "车间下暂无产线");
        Set<Integer> lineIdSet = lineList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        PmsOperateDriverCompleteStorageVO res;
        if (sjgCount != 0) {
            res = sjgComplete(dto, lineIdSet, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH);
        } else {
            res = jpComplete(dto, lineIdSet, UnitLengthUtil.UNIT_TON_LENGTH);
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
     * @param lineIdSet
     * @return
     */
    private PmsOperateDriverCompleteStorageVO sjgInStorage(PmsOperateDriverDTO dto, Set<Integer> lineIdSet, Integer bit) {
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularity, "粒度不存在");
        Map<String, BigDecimal> storageMap = pmsCompleteStorageService.sjgInStorage(granularity, dto.getStart(), dto.getEnd(), lineIdSet, bit);
        Map<String, BigDecimal> actualUseMap = pmsReportWorkService.actualUse(granularity, dto.getStart(), dto.getEnd(), lineIdSet, bit);
        PmsOperateDriverCompleteStorageVO resVo = new PmsOperateDriverCompleteStorageVO();
        //柱状图 + 折线图
        CommonBarLineVO commonBarLineVO = new CommonBarLineVO();
        //根据粒度 分割日期
        List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
        List<String> xAisList = Lists.newArrayList();
        List<BigDecimal> inStorageQuantityList = Lists.newArrayList();
        List<BigDecimal> rateList = Lists.newArrayList();
        List<BigDecimal> actualUseValueList = Lists.newArrayList();
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate ele = dateList.get(i - 1);
            xAisList.add(XAxisUtil.convert(granularity, ele));
            //获取值的方式必须统一
            String key = MysqlDateKeyUtil.convert(granularity, ele);
            BigDecimal storage = storageMap.get(key);
            BigDecimal actualUse = actualUseMap.get(key);
            inStorageQuantityList.add(storage);
            actualUseValueList.add(actualUse);
            rateList.add(BigDecimalUtil.percent(storage, actualUse));
        }
        //计算总量
        resVo.setTotalQuantity(BigDecimalUtil.setScale(BigDecimalUtil.sum(inStorageQuantityList), bit));
        resVo.setTotalRate(BigDecimalUtil.percent(resVo.getTotalQuantity(), BigDecimalUtil.sum(actualUseValueList)));
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
     * @param lineIdSet 组织机构集合
     * @param bit             保留几位
     * @return
     */
    private PmsOperateDriverCompleteStorageVO jpInStorage(PmsOperateDriverDTO dto, Set<Integer> lineIdSet, int bit) {
        //获取粒度
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularity, "粒度不存在");
        //根据车间+时间查询入库数
        //根据车间+时间查询拉引量
        Map<String, BigDecimal> storageMap = pmsCompleteStorageService.jpInStorage(granularity, dto.getStart(), dto.getEnd(), lineIdSet, bit);
        //获取拉引量
        PullForceSumDTO pullForceSumDTO = new PullForceSumDTO();
        pullForceSumDTO.setStart(dto.getStart());
        pullForceSumDTO.setEnd(dto.getEnd());
        pullForceSumDTO.setGranularity(dto.getGranularity());
        pullForceSumDTO.setLineIdSet(lineIdSet);
        Map<String, BigDecimal> pullForceMap = pmsPullForceService.pullForceSum(pullForceSumDTO);
        //柱状图 + 折线图
        CommonBarLineVO commonBarLineVO = new CommonBarLineVO();
        //根据粒度 分割日期
        List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
        List<String> xAisList = Lists.newArrayList();
        List<BigDecimal> inStorageQuantityList = Lists.newArrayList();
        List<BigDecimal> pullForceValueList = Lists.newArrayList();
        List<BigDecimal> rateList = Lists.newArrayList();
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate ele = dateList.get(i - 1);
            xAisList.add(XAxisUtil.convert(granularity, ele));
            //获取值的方式必须统一
            String key = MysqlDateKeyUtil.convert(granularity, ele);
            BigDecimal storage = storageMap.get(key);
            BigDecimal pullForce = pullForceMap.get(key);
            inStorageQuantityList.add(storage);
            pullForceValueList.add(pullForce);
            rateList.add(BigDecimalUtil.percent(storage, pullForce));
        }
        //获取总数
        BigDecimal totalInStorageQuantity = BigDecimalUtil.sum(inStorageQuantityList);
        BigDecimal totalPullForceQuantity = BigDecimalUtil.sum(pullForceValueList);
        PmsOperateDriverCompleteStorageVO resVo = new PmsOperateDriverCompleteStorageVO();
        resVo.setTotalQuantity(totalInStorageQuantity);
        resVo.setTotalRate(BigDecimalUtil.percent(totalInStorageQuantity, totalPullForceQuantity));
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
     * 基片加工产量
     *
     * @param dto             dto
     * @param lineIdSet 组织机构集合
     * @param bit             保留位数
     * @return
     */
    private PmsOperateDriverCompleteStorageVO jpComplete(PmsOperateDriverDTO dto, Set<Integer> lineIdSet, int bit) {
        //获取粒度
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularity, "粒度不存在");
        //报工 统一使用产线

        //根据车间+时间查询入库数
        //根据车间+时间查询拉引量
        Map<String, BigDecimal> completeMap = pmsReportWorkService.jpComplete(granularity, dto.getStart(), dto.getEnd(), lineIdSet, PmManagerConstant.MEASURE_UNIT_TON ,bit);
        PullForceSumDTO pullForceSumDTO = new PullForceSumDTO();
        pullForceSumDTO.setStart(dto.getStart());
        pullForceSumDTO.setEnd(dto.getEnd());
        pullForceSumDTO.setGranularity(dto.getGranularity());
        pullForceSumDTO.setLineIdSet(lineIdSet);
        Map<String, BigDecimal> pullForceMap = pmsPullForceService.pullForceSum(pullForceSumDTO);
        //柱状图 + 折线图
        CommonBarLineVO commonBarLineVO = new CommonBarLineVO();
        //根据粒度 分割日期
        List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
        List<String> xAisList = Lists.newArrayList();
        List<BigDecimal> completeQuantityList = Lists.newArrayList();
        List<BigDecimal> pullForceValueList = Lists.newArrayList();
        List<BigDecimal> rateList = Lists.newArrayList();
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate ele = dateList.get(i - 1);
            xAisList.add(XAxisUtil.convert(granularity, ele));
            //获取值的方式必须统一
            String key = MysqlDateKeyUtil.convert(granularity, ele);
            BigDecimal complete = completeMap.get(key);
            BigDecimal pullForce = pullForceMap.get(key);
            completeQuantityList.add(complete);
            pullForceValueList.add(pullForce);
            rateList.add(BigDecimalUtil.percent(complete, pullForce));
        }
        //计算总计
        BigDecimal totalQuantity = BigDecimalUtil.sum(completeQuantityList);
        BigDecimal totalPullForceQuantity = BigDecimalUtil.sum(pullForceValueList);
        PmsOperateDriverCompleteStorageVO resVo = new PmsOperateDriverCompleteStorageVO();
        resVo.setTotalQuantity(totalQuantity);
        resVo.setTotalRate(BigDecimalUtil.percent(totalQuantity, totalPullForceQuantity));
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
     * @param lineIdSet 组织机构集合
     * @param bit             保留位数
     * @return
     */
    private PmsOperateDriverCompleteStorageVO sjgComplete(PmsOperateDriverDTO dto, Set<Integer> lineIdSet, int bit) {
        //获取粒度
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(dto.getGranularity());
        Assert.notNull(granularity, "粒度不存在");
        //根据车间+时间查询完工数
        Map<String, BigDecimal> completeMap = pmsReportWorkService.sjgComplete(granularity, dto.getStart(), dto.getEnd(), lineIdSet, bit);
        //根据车间+时间查询实际使用量
        Map<String, BigDecimal> actualUseMap = pmsReportWorkService.actualUse(granularity, dto.getStart(), dto.getEnd(), lineIdSet, bit);
        //柱状图 + 折线图
        CommonBarLineVO commonBarLineVO = new CommonBarLineVO();
        //根据粒度 分割日期
        List<LocalDate> dateList = ShardTimeUtil.cutDate(granularity.getType(), dto.getStart(), dto.getEnd());
        List<String> xAisList = Lists.newArrayList();
        List<BigDecimal> completeQuantityList = Lists.newArrayList();
        List<BigDecimal> actualValueList = Lists.newArrayList();
        List<BigDecimal> rateList = Lists.newArrayList();
        for (int i = 1; i < dateList.size(); i++) {
            LocalDate ele = dateList.get(i - 1);
            xAisList.add(XAxisUtil.convert(granularity, ele));
            //获取值的方式必须统一
            String key = MysqlDateKeyUtil.convert(granularity, ele);
            BigDecimal complete = completeMap.get(key);
            BigDecimal actualUse = actualUseMap.get(key);
            completeQuantityList.add(complete);
            actualValueList.add(actualUse);
            rateList.add(BigDecimalUtil.percent(complete, actualUse));
        }
        //计算总计
        BigDecimal totalQuantity = BigDecimalUtil.sum(completeQuantityList);
        BigDecimal totalActualUseQuantity = BigDecimalUtil.sum(actualValueList);
        PmsOperateDriverCompleteStorageVO resVo = new PmsOperateDriverCompleteStorageVO();
        resVo.setTotalQuantity(totalQuantity);
        resVo.setTotalRate(BigDecimalUtil.percent(totalQuantity, totalActualUseQuantity));

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
