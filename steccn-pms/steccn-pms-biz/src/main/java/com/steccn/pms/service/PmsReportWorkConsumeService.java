package com.steccn.pms.service;

import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.domain.PmsReportWorkConsume;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsDayReportDTO;
import com.steccn.pms.dto.ProcessReportWorkConsumeDTO;
import com.steccn.pms.vo.EnergyGroupListVO;
import com.steccn.pms.vo.SubstrateDayReportListVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yequan
 * @description 针对表【pms_report_work_consume(报工能耗表)】的数据库操作Service
 * @createDate 2022-11-02 15:32:08
 */
public interface PmsReportWorkConsumeService extends IService<PmsReportWorkConsume> {

    /**
     * 保存能耗信息
     *
     * @param id             报工主键id
     * @param consumeDTOList 能耗信息
     */
    void saveConsume(Long id, List<ProcessReportWorkConsumeDTO> consumeDTOList);

    /**
     * 水电油气能耗分组
     *
     * @param productLineIdList 产线id
     * @param dateRangeList     时间范围
     * @return 分组集合
     */
    List<EnergyGroupListVO> energyGroup(List<Integer> productLineIdList, List<LocalDateTime> dateRangeList);


    /**
     * 基片日报表能耗
     *
     * @param dto         dto筛选条件
     * @param classList   班次数据
     * @param productLine 产线
     * @return list
     */
    List<SubstrateDayReportListVO> substrateEnergy(PmsDayReportDTO dto, List<SysDictVO> classList, SysOfficeVO productLine);
}
