package com.steccn.ems.service;

import com.steccn.ems.domain.EpEnergyPricePlan;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.dto.EpDwEnergyPricePlanDTO;
import com.steccn.ems.dto.EpEnergyPriceDTO;
import com.steccn.ems.vo.EpEnergyPriceVO;

import java.time.LocalDate;
import java.util.List;

/**
* @author yequan
* @description 针对表【ep_energy_price_plan(能源价格方案)】的数据库操作Service
* @createDate 2022-11-29 19:27:28
*/
public interface EpEnergyPricePlanService extends IService<EpEnergyPricePlan> {

    /**
     * 编辑或新增能源价格方案
     * @param dto
     * @return
     */
    Boolean addOrEdit(EpEnergyPriceDTO dto);

    /**
     * 查询所有能源价格方案
     * @return
     */
    List<EpEnergyPriceVO> getEpEnergyPriceVOList();

    /**
     * 根据id查询能源价格方案
     * @param id
     * @return
     */
    EpEnergyPriceVO getEpEnergyPriceVO(Long id);

    /**
     * 根据id删除能源价格方案
     * @param id
     * @return
     */
    Boolean delete(Long id);


    /**
     * 查询指定班次日期有效的能源计费方案明细
     * @param date 指定班次日期
     * @return 能源计费方案明细
     */
    List<EpDwEnergyPricePlanDTO> currentValidPlan(LocalDate date);
}
