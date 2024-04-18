package com.steccn.ems.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
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

    Page<CeFossilFuelSettingVO> getPage(CeFossilFuelSettingPageDTO dto);

    CeFossilFuelSettingVO info(Integer id);

    Boolean add(CeFossilFuelSettingDTO dto);

    Boolean edit(CeFossilFuelSettingDTO dto);
}
