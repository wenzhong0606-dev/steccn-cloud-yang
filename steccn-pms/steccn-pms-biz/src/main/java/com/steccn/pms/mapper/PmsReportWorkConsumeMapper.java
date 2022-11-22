package com.steccn.pms.mapper;

import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.domain.PmsReportWorkConsume;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.vo.EnergyGroupListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.security.core.parameters.P;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yequan
 * @description 针对表【pms_report_work_consume(报工能耗表)】的数据库操作Mapper
 * @createDate 2022-11-02 15:32:08
 * @Entity com.steccn.pms.domain.PmsReportWorkConsume
 */
@Mapper
public interface PmsReportWorkConsumeMapper extends BaseMapper<PmsReportWorkConsume> {

    /**
     * 根据产线分组水电油气能耗
     *
     * @param productLineIdList 产线id集合
     * @param dateRangeList     日期范围
     * @return list
     */
    List<EnergyGroupListVO> energyGroup(@Param("productLineIdList") List<Integer> productLineIdList, @Param("dateRangeList") List<LocalDateTime> dateRangeList);

    /**
     * 基片日报表
     * @param classList 班次id集合
     * @param productLineId 产线id
     * @param date 日期
     * @return 数据
     */
    List<EnergyGroupListVO> substrateEnergy(@Param("classList") List<Long> classList, @Param("productLineId") Integer productLineId, @Param("date") LocalDate date);
}




