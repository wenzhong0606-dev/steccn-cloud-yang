package com.steccn.pms.service.impl;

import cn.hutool.core.lang.Assert;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.StackGraphDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.StackGraphUtil;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.service.PmsSjgReportDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

    @Override
    public QtLineChartBarDTO lineProduce(Integer workShopId, LocalDate date) {
        //产量：钢化工序的合格数（加工数-不良数）
        //入库：选择时间端内，该产线入库数量汇总
        //耗用：磨边工序的加工数

        List<SysOfficeVO> wsLineList = getWsLineList(workShopId);
        Assert.notNull(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());

        Map<Long, Map<Integer, BigDecimal>> todayPmMap = reportWorkService.getTodayPm(lineIdList, date, 1);

        //定义数据集合
        List<StackGraphDTO<String, Integer>> stackGraphDTOList = Lists.newArrayList();
        wsLineList.forEach(ele -> {
            StackGraphDTO<String, Integer> stackGraphDTO = new StackGraphDTO<>();
            stackGraphDTO.setXAxis(ele.getOfficeName());
            List<TupleDTO<String, Integer>> dataList = Lists.newArrayList();
            //产量，入库，耗用
            dataList.add(new TupleDTO<>("产量", null));
            dataList.add(new TupleDTO<>("入库", null));
            dataList.add(new TupleDTO<>("耗用", null));
            stackGraphDTO.setData(dataList);
            stackGraphDTOList.add(stackGraphDTO);
        });
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
