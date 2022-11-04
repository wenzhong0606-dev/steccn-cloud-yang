package com.steccn.pms.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.constant.PmReportConstant;
import com.steccn.pms.dto.EnergyCountDTO;
import com.steccn.pms.dto.SubstrateDayReportDTO;
import com.steccn.pms.service.ReportService;
import com.steccn.pms.vo.EnergyCountVO;
import com.steccn.pms.vo.SubstrateDayReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * @author yequan
 * @desc 报表接口实现
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final RemoteOfficeFeign remoteOfficeFeign;

    @Override
    public SubstrateDayReportVO substrateDayReport(SubstrateDayReportDTO dto) {
        return null;
    }

    @Override
    public EnergyCountVO consume(EnergyCountDTO dto) {
        Assert.isTrue(PmReportConstant.DataGaugeTypeEnum.all().contains(dto.getDataGaugeType()), "统计维度不存在");
        //获取产线数据
        List<SysOfficeVO> productLineList = productLineList(dto.getWorkshopId());
        Assert.notNull(productLineList, "车间下暂无产线");
        //根据产线id查询分组数据

        return null;
    }

    /**
     * 根据车间去查询所有产线
     *
     * @param workShopId 车间id
     * @return
     */
    private List<SysOfficeVO> productLineList(Integer workShopId) {
        SysOfficeParentDTO sysOfficeParentDTO = new SysOfficeParentDTO();
        sysOfficeParentDTO.setId(workShopId);
        sysOfficeParentDTO.setOfficeType(PmManagerConstant.STATE_13);
        return FeignOptional.of(remoteOfficeFeign.listByParentId(sysOfficeParentDTO)).get();
    }

}
