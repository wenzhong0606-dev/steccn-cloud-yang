package com.steccn.pms.service.impl;

import cn.hutool.core.lang.Assert;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.EpChartGraphxAxisDTO;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.StackGraphDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.StackGraphUtil;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.service.PmsSjgReportDashboardService;
import com.steccn.pms.vo.SjgDashboardLineProduceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
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
 * @desc 深加工车间看板接口
 * @date 2023/9/5
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PmsSjgReportDashboardServiceImpl implements PmsSjgReportDashboardService {

    private final PmsReportWorkServiceImpl reportWorkService;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final RemoteDictFeignApi remoteDictFeignApi;

    @Override
    public QtLineChartBarDTO lineProduce(Integer workShopId, LocalDate start, LocalDate end, String unit, Integer bit) {
        //产量：钢化工序的合格数（加工数-不良数）
        //入库：选择时间端内，该产线入库数量汇总
        //耗用：磨边工序的加工数
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notNull(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());

        Map<Integer, BigDecimal> dateRangePmMap = reportWorkService.getDateRangePmUnit(lineIdList, start, end, 1, unit);
        Map<Integer, BigDecimal> dateRangeInStorageMap = reportWorkService.getDateRangeInStorageUnit(lineIdList, start, end, unit);
        Map<Integer, BigDecimal> dateRangeUseCountMap = reportWorkService.getDateRangeUseCountUnit(lineIdList, start, end, unit);

        //定义数据集合
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        wsLineList.forEach(ele -> {
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            stackGraphDTO.setXAxis(ele.getOfficeName());
            List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
            //产量，入库，耗用
            dataList.add(new TupleDTO<>("产量", BigDecimalUtil.setScale(dateRangePmMap.get(ele.getId()), bit)));
            dataList.add(new TupleDTO<>("入库", BigDecimalUtil.setScale(dateRangeInStorageMap.get(ele.getId()), bit)));
            dataList.add(new TupleDTO<>("耗用", BigDecimalUtil.setScale(dateRangeUseCountMap.get(ele.getId()), bit)));
            stackGraphDTO.setData(dataList);
            stackGraphDTOList.add(stackGraphDTO);
        });
        //自定义转换工具
        return StackGraphUtil.convertData(stackGraphDTOList);
    }

    @Override
    public SjgDashboardLineProduceVO lineProduceDetail(Integer workShopId, LocalDate start, LocalDate end, String unit, Integer bit) {
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notNull(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        SjgDashboardLineProduceVO resVo = new SjgDashboardLineProduceVO();
        Map<Integer, BigDecimal> dateRangePmMap = reportWorkService.getDateRangePmUnit(lineIdList, start, end, 1, unit);
        Map<Integer, BigDecimal> dateRangeInStorageMap = reportWorkService.getDateRangeInStorageUnit(lineIdList, start, end, unit);
        Map<Integer, BigDecimal> dateRangeUseCountMap = reportWorkService.getDateRangeUseCountUnit(lineIdList, start, end, unit);
        List<String> headList = Lists.newArrayList();
        List<BigDecimal> pmList = Lists.newArrayList();
        List<BigDecimal> inStorageList = Lists.newArrayList();
        List<BigDecimal> consumeList = Lists.newArrayList();
        wsLineList.forEach(ele -> {
            headList.add(ele.getOfficeName());
            pmList.add(BigDecimalUtil.setScale(dateRangePmMap.get(ele.getId()), bit));
            inStorageList.add(BigDecimalUtil.setScale(dateRangeInStorageMap.get(ele.getId()), bit));
            consumeList.add(BigDecimalUtil.setScale(dateRangeUseCountMap.get(ele.getId()), bit));
        });
        resVo.setHeadList(headList);
        resVo.setPmList(pmList);
        resVo.setInStorageList(inStorageList);
        resVo.setConsumeList(consumeList);
        return resVo;
    }

    @Override
    public QtLineChartBarDTO lineProduceClassGroup(Integer workShopId, LocalDate start, LocalDate end, String unit, Integer bit) {
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notNull(wsLineList, "车间下暂无产线");
        Set<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        Map<String, BigDecimal> pmMap = reportWorkService.lineClassPm(lineIdList, start, end, 1, unit);
        Map<String, BigDecimal> inStorageMap = reportWorkService.lineClassInStorage(lineIdList, start, end, unit);
        Map<String, BigDecimal> useCountMap = reportWorkService.lineClassUseCount(lineIdList, start, end, unit);
        //获取白班与夜班
        List<SysDictVO> sysDictList = FeignOptional.of(() -> remoteDictFeignApi.listByType(PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        //组装数据
        List<String> xAxisList1 = Lists.newArrayList();
        List<String> xAxisList2 = Lists.newArrayList();
        for (SysOfficeVO line : wsLineList) {
            for (SysDictVO sysDict : sysDictList) {
                StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
                stackGraphDTO.setXAxis(sysDict.getName());
                List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
                //产量，入库，耗用
                dataList.add(new TupleDTO<>("产量", BigDecimalUtil.setScale(pmMap.get(line.getId() + "_" + sysDict.getId()), bit)));
                dataList.add(new TupleDTO<>("入库", BigDecimalUtil.setScale(inStorageMap.get(line.getId() + "_" + sysDict.getId()), bit)));
                dataList.add(new TupleDTO<>("耗用", BigDecimalUtil.setScale(useCountMap.get(line.getId() + "_" + sysDict.getId()), bit)));
                stackGraphDTO.setData(dataList);
                stackGraphDTOList.add(stackGraphDTO);
                xAxisList2.add(sysDict.getName());
            }
            xAxisList1.add(line.getOfficeName());
        }
        //自定义转换工具
        QtLineChartBarDTO qtLineChartBarDTO = StackGraphUtil.convertData(stackGraphDTOList);
        qtLineChartBarDTO.setXData(Arrays.asList(xAxisList1, xAxisList2));
        return qtLineChartBarDTO;
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
