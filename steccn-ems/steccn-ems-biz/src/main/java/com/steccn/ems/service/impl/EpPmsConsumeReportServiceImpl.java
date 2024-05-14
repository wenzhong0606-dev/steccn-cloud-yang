package com.steccn.ems.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.SteccnChartException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.GetValueUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.dp.dto.DpPullForceDTO;
import com.steccn.dp.feign.RemoteDpPullForceFeignApi;
import com.steccn.ems.domain.EpEnergyType;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.service.*;
import com.steccn.ems.util.UnitConsumeUtil;
import com.steccn.ems.vo.DeepProcessConsumeReportVO;
import com.steccn.ems.vo.EpMeterReadAllDataVO;
import com.steccn.ems.vo.EpPmsConsumeVO;
import com.steccn.ems.vo.PmElectricConsumeVO;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.dto.PmsOperateDriverDTO;
import com.steccn.pms.dto.PullForceSumDTO;
import com.steccn.pms.feign.RemoteCompleteStorageFeignApi;
import com.steccn.pms.feign.RemotePullForceFeignApi;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.MysqlDateKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @desc 生产看板能源服务
 * @date 2023/8/18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EpPmsConsumeReportServiceImpl implements EpPmsConsumeReportService {

    private final EpEnergyTypeService epEnergyTypeService;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final EpPartitionService epPartitionService;
    private final EpMeterReadService epMeterReadService;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final EmsOfficeManager emsOfficeManager;
    private final RemotePullForceFeignApi remotePullForceFeignApi;
    private final RemoteCompleteStorageFeignApi remoteCompleteStorageFeignApi;

    @Override
    public EpPmsConsumeVO consumeUseElectric(Integer officeId, LocalDate start, LocalDate end) {
        return consumeUse(officeId, start, end, 1);
    }

    @Override
    public EpPmsConsumeVO consumeUseWater(Integer officeId, LocalDate start, LocalDate end) {
        return consumeUse(officeId, start, end, 2);
    }

    @Override
    public EpPmsConsumeVO consumeUseLng(Integer officeId, LocalDate start, LocalDate end) {
        return consumeUse(officeId, start, end, 8);
    }

    @Override
    public EpPmsConsumeVO consumeUseGas(Integer officeId, LocalDate start, LocalDate end) {
        return consumeUse(officeId, start, end, 3);
    }

    @Override
    public EpPmsConsumeVO consumeUseOil(Integer officeId, LocalDate start, LocalDate end) {
        EpPmsConsumeVO epPmsConsumeVO = consumeUse(officeId, start, end, 4);
        epPmsConsumeVO.setSum(BigDecimalUtil.setScale(epPmsConsumeVO.getSum(), 1));
        return epPmsConsumeVO;
    }

    @Override
    public EpPmsConsumeVO consumeUseUnitConsume(Integer officeId, LocalDate start, LocalDate end) {
        //获取office的数据
        SysOfficeVO sysOfficeVO = FeignOptional.of(() -> remoteOfficeFeign.info(officeId), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(sysOfficeVO, "组织机构不存在");
        Set<Integer> workshopIdSet = Sets.newHashSet();
        if (SysOfficeConstant.DEPARTMENT.equals(sysOfficeVO.getOfficeType())) {
            List<SysOfficeVO> workshopList = emsOfficeManager.getChildren(sysOfficeVO.getId(), SysOfficeConstant.WORKSHOP);
            Assert.notEmpty(workshopList, "制造部下暂无车间");
            List<Integer> wsIdList = GetValueUtil.collectionGet(workshopList, x -> x.getId());
            workshopIdSet.addAll(wsIdList);
        } else {
            workshopIdSet.add(sysOfficeVO.getId());
        }
        if(sysOfficeVO.getOfficeName().contains("深加工")){
            //深加工单耗：电耗/入库产量
            EpPmsConsumeVO electric = consumeUseElectric(officeId, start, end);
            PmsOperateDriverDTO pmsOperateDriverDTO = new PmsOperateDriverDTO();
            pmsOperateDriverDTO.setStart(start);
            pmsOperateDriverDTO.setEnd(end);
            pmsOperateDriverDTO.setGranularity(QtManagerConstant.QtGranularityEnum.YEAR.getCode());
            pmsOperateDriverDTO.setOfficeId(sysOfficeVO.getId());
            Map<String, BigDecimal> inStorageMap = FeignOptional.of(() -> remoteCompleteStorageFeignApi.sjgInStorageSum(pmsOperateDriverDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            BigDecimal sum = BigDecimalUtil.sum(inStorageMap.values());
            EpPmsConsumeVO res = new EpPmsConsumeVO();
            res.setUnitName("kwh/㎡");
            res.setSum(BigDecimalUtil.divide(electric.getSum(), sum, 1));
            return res;

        }else{
            EpPmsConsumeVO oil = consumeUseOil(officeId, start, end);
            EpPmsConsumeVO gas = consumeUseGas(officeId, start, end);
            EpPmsConsumeVO lng = consumeUseLng(officeId, start, end);
            PullForceSumDTO pullForceSumDTO = new PullForceSumDTO();
            pullForceSumDTO.setWorkshopIdSet(workshopIdSet);
            pullForceSumDTO.setGranularity(QtManagerConstant.QtGranularityEnum.NONE.getCode());
            pullForceSumDTO.setStart(start);
            pullForceSumDTO.setEnd(end);
            Map<String, BigDecimal> pullForceMap = FeignOptional.of(() -> remotePullForceFeignApi.pullForceSum(pullForceSumDTO), FeignConstant.STECCN_DP_SERVICE_NAME).get();
            //（天然气 * 7800 + 重油 * 9300 + LNG * 8000）/ 拉引量 * 1000
            BigDecimal pullForce = pullForceMap.get(MysqlDateKeyUtil.convert(QtManagerConstant.QtGranularityEnum.NONE, start));
            BigDecimal sum = UnitConsumeUtil.unitConsume(gas.getSum(), oil.getSum(), lng.getSum(), pullForce, 1);
            EpPmsConsumeVO res = new EpPmsConsumeVO();
            res.setUnitName("kcal/kg");
            res.setSum(sum);
            return res;
        }
    }

    @Override
    public List<DeepProcessConsumeReportVO> deepProcessConsumeReport(LocalDate start, LocalDate end) {
        List<DeepProcessConsumeReportVO> results = Lists.newArrayList();
        Map<String,BigDecimal> count = Maps.newHashMap();
        //拿到三个车间的用电量(目前没有一车间,二车间79,三车间193)
        DeepProcessConsumeReportVO electricResult = new DeepProcessConsumeReportVO();
        EpPmsConsumeVO electric2 = consumeUseElectric(79, start, end);
        EpPmsConsumeVO electric3 = consumeUseElectric(193,start,end);
        BigDecimalUtil.accumulate(count,"electric",electric2.getSum());
        BigDecimalUtil.accumulate(count,"electric",electric3.getSum());
        electricResult.setCategory("电(Kwh)");
        electricResult.setTwo(electric2.getSum());
        electricResult.setThree(electric3.getSum());
        electricResult.setCount(count.get("electric"));
        results.add(electricResult);
        //用水量
        DeepProcessConsumeReportVO waterResult = new DeepProcessConsumeReportVO();
        EpPmsConsumeVO water2 = consumeUseWater(79, start, end);
        EpPmsConsumeVO water3 = consumeUseWater(193,start,end);
        BigDecimalUtil.accumulate(count,"water",water2.getSum());
        BigDecimalUtil.accumulate(count,"water",water3.getSum());
        waterResult.setCategory("水(T)");
        waterResult.setTwo(water2.getSum());
        waterResult.setThree(water3.getSum());
        waterResult.setCount(count.get("water"));
        results.add(waterResult);
        //单耗
        //深加工单耗：电耗/入库产量(获取入库产量)
        PmsOperateDriverDTO pmsOperateDriverDTO = new PmsOperateDriverDTO();
        pmsOperateDriverDTO.setStart(start);
        pmsOperateDriverDTO.setEnd(end);
        pmsOperateDriverDTO.setGranularity(QtManagerConstant.QtGranularityEnum.YEAR.getCode());
        pmsOperateDriverDTO.setOfficeId(79);
        Map<String, BigDecimal> inStorageMap = FeignOptional.of(() -> remoteCompleteStorageFeignApi.sjgInStorageSum(pmsOperateDriverDTO), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        BigDecimal sum2 = BigDecimalUtil.sum(inStorageMap.values());
        BigDecimalUtil.accumulate(count,"inStorage",sum2);
        pmsOperateDriverDTO.setOfficeId(193);
        inStorageMap = FeignOptional.of(() -> remoteCompleteStorageFeignApi.sjgInStorageSum(pmsOperateDriverDTO), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        BigDecimal sum3 = BigDecimalUtil.sum(inStorageMap.values());
        BigDecimalUtil.accumulate(count,"inStorage",sum3);
        DeepProcessConsumeReportVO unitConsumeResult = new DeepProcessConsumeReportVO();
        unitConsumeResult.setCategory("单位电耗(kwh/㎡)");
        unitConsumeResult.setTwo(BigDecimalUtil.divide(electric2.getSum(),sum2,1));
        unitConsumeResult.setThree(BigDecimalUtil.divide(electric3.getSum(),sum3,1));
        unitConsumeResult.setCount(BigDecimalUtil.divide(count.get("electric"),count.get("inStorage"),1));
        results.add(unitConsumeResult);
        return results;
    }

    private EpPmsConsumeVO consumeUse(Integer officeId, LocalDate start, LocalDate end, Integer type) {
        //获取单位
        EpEnergyType epEnergyType = epEnergyTypeService.getById(type);
        Assert.notNull(epEnergyType, "能源信息为空");
        Map<Long, String> measureUnitMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(Sets.newHashSet(epEnergyType.getMeasureUnit())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        EpPartition nodeInfo = epPartitionService.getOne(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getOfficeId, officeId).eq(EpPartition::getType, epEnergyType.getId()), Boolean.FALSE);
        if (Objects.isNull(nodeInfo)) {
            throw new SteccnChartException("当前节点不存在于能源树");
        }
        List<LocalDate> cutDateList = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), start, end);
        cutDateList.remove(cutDateList.size() - 1);
        Set<LocalDate> dateSet = Sets.newHashSet(cutDateList);
        List<EpMeterReadAllDataVO> classDateAllData = epMeterReadService.classDateAllData(dateSet, epEnergyType.getId(), Arrays.asList(nodeInfo.getId()));
        List<BigDecimal> collect = classDateAllData.stream().map(EpMeterReadAllDataVO::getConsume).collect(Collectors.toList());
        return new EpPmsConsumeVO(BigDecimalUtil.sum(collect), measureUnitMap.get(epEnergyType.getMeasureUnit()));
    }
}
