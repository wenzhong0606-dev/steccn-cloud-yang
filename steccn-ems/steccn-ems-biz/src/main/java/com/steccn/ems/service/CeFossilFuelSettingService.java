package com.steccn.ems.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.ems.domain.CeFossilFuelSetting;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.dto.CeFossilFuelSettingDTO;
import com.steccn.ems.dto.CeFossilFuelSettingPageDTO;
import com.steccn.ems.vo.CeFossilFuelSettingVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @description 针对表【ce_fossil_fuel_setting(化石燃料设置表)】的数据库操作Service
 * @createDate 2024-04-18 09:27:24
 */
public interface CeFossilFuelSettingService extends IService<CeFossilFuelSetting> {

    /**
     * 查询化石燃料设置分页列表
     *
     * @param dto
     * @return
     */
    Page<CeFossilFuelSettingVO> getPage(CeFossilFuelSettingPageDTO dto);

    /**
     * 根据id查询化石燃料设置
     *
     * @param id
     * @return
     */
    CeFossilFuelSettingVO info(Integer id);

    /**
     * 新增化石燃料设置
     *
     * @param dto
     * @return
     */
    Boolean add(CeFossilFuelSettingDTO dto);

    /**
     * 编辑化石燃料设置
     *
     * @param dto
     * @return
     */
    Boolean edit(CeFossilFuelSettingDTO dto);

    /**
     * 碳排放总量报表-按日为粒度
     * @param start 开始日期
     * @param end 结束日期
     * @return
     */
    List<Map<LocalDate, BigDecimal>> carbonSumReportDay(LocalDate start, LocalDate end);
}
