package com.steccn.pms.service.impl;

import cn.hutool.core.lang.Assert;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.StackGraphDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.common.util.StackGraphUtil;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.DashboardNumDTO;
import com.steccn.pms.service.PmsPullForceService;
import com.steccn.pms.service.PmsReportDashboardCtService;
import com.steccn.pms.service.PmsReportWorkService;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.util.XAxisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
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


    @Override
    public QtLineChartBarDTO produceClass(Integer workShopId, LocalDate start, LocalDate end, TupleDTO<String, Integer> unit) {
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end.plusDays(1));
        List<LocalDateTime> timeList = ShardTimeUtil.cutDateRangeNew("D", startLdt, endLdt);
        Assert.notEmpty(timeList, "暂无时间窗口");
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notNull(wsLineList, "车间下暂无产线");
        Set<Integer> lineIdSet = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        //获取基片的每日产量
        Map<String, BigDecimal> pmMap = reportWorkService.lineClassPm(lineIdSet, start, end, 0, unit.getField1());

        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        for (int i = 1; i < timeList.size(); i++) {
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            stackGraphDTO.setXAxis(XAxisUtil.convert(QtManagerConstant.QtGranularityEnum.DAY, timeList.get(i - 1)));
            List<TupleDTO<String, BigDecimal>> data = Lists.newArrayList();
            BigDecimal classDateSum = null;
            LocalDate classDate = LocalDateUtil.getClassDate(timeList.get(i - 1), Boolean.FALSE);
            for (Integer line : lineIdSet) {
                classDateSum = BigDecimalUtil.add(classDateSum, pmMap.get(line + "_" + classDate));
            }
            data.add(new TupleDTO<>("累计总产量", classDateSum));
            stackGraphDTO.setData(data);
            stackGraphDTOList.add(stackGraphDTO);
        }
        //自定义转换工具
        return StackGraphUtil.convertData(stackGraphDTOList);
    }

    @Override
    public QtLineChartBarDTO yieldRateDate(Integer workShopId, LocalDate start, LocalDate end) {
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end.plusDays(1));
        List<LocalDateTime> timeList = ShardTimeUtil.cutDateRangeNew("D", startLdt, endLdt);
        Assert.notEmpty(timeList, "暂无时间窗口");
        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notNull(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());

        Map<String, BigDecimal> pmMap = reportWorkService.datePmUnit(lineIdList, start, end, 0, PmManagerConstant.MEASURE_UNIT_TON);
        Map<String, BigDecimal> inStorageMap = reportWorkService.dateInStorageUnit(lineIdList, start, end, PmManagerConstant.MEASURE_UNIT_TON);
        Map<String, BigDecimal>  forceMap = pmsPullForceService.datePullForce(lineIdList, start, end);

        //定义数据集合
        List<StackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();
        for (int i = 1; i < timeList.size(); i++) {
            LocalDate classDate = LocalDateUtil.getClassDate(timeList.get(i - 1), Boolean.FALSE);
            StackGraphDTO<String, BigDecimal> stackGraphDTO = new StackGraphDTO<>();
            stackGraphDTO.setXAxis(XAxisUtil.convert(QtManagerConstant.QtGranularityEnum.DAY, timeList.get(i - 1)));
            List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
            //产量，入库，耗用
            dataList.add(new TupleDTO<>("完工总成品率(%)", BigDecimalUtil.percent(pmMap.get(classDate + ""), forceMap.get(classDate + ""))));
            dataList.add(new TupleDTO<>("入库总成品率(%)", BigDecimalUtil.percent(inStorageMap.get(classDate + ""), forceMap.get(classDate + ""))));
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

    /**
     * 班次产量，入库量，拉引量
     * *
     *
     * @param numDTO 条件
     * @param function 函数
     * @return
     */
    private QtLineChartBarDTO num(String hint, DashboardNumDTO numDTO, Function<DashboardNumDTO, Map<String, BigDecimal>> function) {
        List<LocalDate> timeList = ShardTimeUtil.cutDate("D", numDTO.getStart(), numDTO.getEnd());
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
            stackGraphDTO.setXAxis(XAxisUtil.convert(QtManagerConstant.QtGranularityEnum.DAY, timeList.get(i - 1)));
            List<TupleDTO<String, BigDecimal>> dataList = Lists.newArrayList();
            //产量，入库，耗用
            dataList.add(new TupleDTO<>(hint, BigDecimalUtil.setScale(pmMap.get(classDate + ""), numDTO.getBit())));
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
