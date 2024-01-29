package com.steccn.ems.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.CommonReportDTO;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.dto.EpMeterReadAddDTO;
import com.steccn.ems.dto.EpMeterReadUpdateDTO;
import com.steccn.ems.vo.EpMeterReadAllDataVO;
import com.steccn.ems.vo.EpMeterReadDetailsVO;
import com.steccn.ems.vo.EpMeterReadVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author yequan
* @description 针对表【ep_meter_read(抄表)】的数据库操作Service
* @createDate 2022-11-30 15:05:32
*/
public interface EpMeterReadService extends IService<EpMeterRead> {

    /**
     * 抄表管理列表
     *
     * @param readMonth 抄表月份
     * @param meterType 仪表类型
     * @return
     */
    EpMeterReadVO list(String readMonth, Integer meterType);

    /**
     * 录入抄表数据
     *
     * @param dto 入参
     * @return
     */
    Boolean add(EpMeterReadAddDTO dto);

    /**
     * 抄表详情
     *
     * @param readDate
     * @param meterId
     * @return
     */
    EpMeterReadDetailsVO details(String readDate, Integer meterId);

    /**
     * 批量修改
     *
     * @param dtoList
     * @return
     */
    Boolean updateBatch(List<EpMeterReadUpdateDTO> dtoList);


    /**
     * 班次日期和组织机构筛选条件下的全量数据表
     *
     * @param dateList      日期集合
     * @param energyType    能源类型id
     * @param partitionIdList  机构id集合
     * @return 全量数据表
     */
    List<EpMeterReadAllDataVO> classDateAllData(Set<LocalDate> dateList, Integer energyType, List<Long> partitionIdList);

    /**
     * 班次日期和组织机构筛选条件下的全量数据表-电能单耗
     *
     * @param dateList      日期集合
     * @param energyType    能源类型id
     * @param partitionIdList  机构id集合
     * @return 全量数据表-单耗
     */
    List<EpMeterReadAllDataVO> classDateAllDataUnit(Set<LocalDate> dateList, Integer energyType, List<Long> partitionIdList);

    /**
     * 班次日期和组织机构筛选条件下的全量数据表-其余能源单耗
     *
     * @param dateSet           日期集合
     * @param energyType      能源类型id
     * @param partitionIdList 机构id集合
     * @return 全量数据表-单耗
     */
    List<EpMeterReadAllDataVO> classDateAllDataUnitOther(Set<LocalDate> dateSet, Integer energyType, List<Long> partitionIdList);

    /**
     * 燃料(气+油)的单耗
     * @param dateSet 日期数据
     * @param partitionIdList 分区节点id集合
     * @return list
     */
    List<EpMeterReadAllDataVO> kcalDataUnit(Set<LocalDate> dateSet, List<Long> partitionIdList);

    /**
     * 35kV开关站日报表
     * @param readMonth 月份
     * @param energyType 能源类型
     * @return
     */
    EpMeterReadVO kvDay(String readMonth, Integer energyType);

    /**
     * 获取指定能源的耗用数
     *
     * @param energyType     能源类型
     * @param workshopId     车间主键
     * @param start          开始日期
     * @param end            结束日期
     * @param granularityInt 粒度
     * @return
     */
    CommonReportDTO energyConsume(Integer energyType, Integer workshopId, LocalDate start, LocalDate end, Integer granularityInt);


    /**
     * 获取指定能源的能耗
     *
     * @param energyType     能源类型
     * @param officeIdList   组织架构主键
     * @param start          开始日期
     * @param end            结束日期
     * @param granularityInt 粒度
     * @return
     */
    Map<String, BigDecimal> energyConsumeOffice(Integer energyType, List<Integer> officeIdList, LocalDate start, LocalDate end, Integer granularityInt);

    /**
     * 获取指定能源的单耗
     *
     * @param energyType     能源类型
     * @param officeIdList   组织架构主键
     * @param start          开始日期
     * @param end            结束日期
     * @param granularityInt 粒度
     * @return
     */
    Map<String, BigDecimal> unitConsumeOffice(Integer energyType, List<Integer> officeIdList, LocalDate start, LocalDate end, Integer granularityInt);
}
