package com.steccn.ems.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.dto.EpMeterReadAddDTO;
import com.steccn.ems.dto.EpMeterReadUpdateDTO;
import com.steccn.ems.vo.EpMeterReadAllDataVO;
import com.steccn.ems.vo.EpMeterReadDetailsVO;
import com.steccn.ems.vo.EpMeterReadVO;

import java.time.LocalDate;
import java.util.List;
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
     * @param officeIdList  机构id集合
     * @return 全量数据表
     */
    List<EpMeterReadAllDataVO> classDateAllData(Set<LocalDate> dateList, Integer energyType, List<Integer> officeIdList);
}
