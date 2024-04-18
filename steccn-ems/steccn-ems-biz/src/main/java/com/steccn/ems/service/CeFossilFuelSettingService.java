package com.steccn.ems.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.ems.domain.CeFossilFuelSetting;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.dto.CeFossilFuelSettingDTO;
import com.steccn.ems.dto.CeFossilFuelSettingPageDTO;
import com.steccn.ems.vo.CeFossilFuelSettingVO;

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
}
