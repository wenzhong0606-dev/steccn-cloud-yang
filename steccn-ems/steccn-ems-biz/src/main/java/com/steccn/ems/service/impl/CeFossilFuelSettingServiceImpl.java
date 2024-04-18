package com.steccn.ems.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.ems.domain.CeFossilFuelSetting;
import com.steccn.ems.dto.CeFossilFuelSettingDTO;
import com.steccn.ems.dto.CeFossilFuelSettingPageDTO;
import com.steccn.ems.service.CeFossilFuelSettingService;
import com.steccn.ems.mapper.CeFossilFuelSettingMapper;
import com.steccn.ems.vo.CeFossilFuelSettingVO;
import org.springframework.stereotype.Service;

/**
* @author yequan
* @description 针对表【ce_fossil_fuel_setting(化石燃料设置表)】的数据库操作Service实现
* @createDate 2024-04-18 09:27:24
*/
@Service
public class CeFossilFuelSettingServiceImpl extends ServiceImpl<CeFossilFuelSettingMapper, CeFossilFuelSetting>
    implements CeFossilFuelSettingService{

    @Override
    public Page<CeFossilFuelSettingVO> getPage(CeFossilFuelSettingPageDTO dto) {
        return null;
    }

    @Override
    public CeFossilFuelSettingVO info(Integer id) {
        return null;
    }

    @Override
    public Boolean add(CeFossilFuelSettingDTO dto) {
        return null;
    }

    @Override
    public Boolean edit(CeFossilFuelSettingDTO dto) {
        return null;
    }
}




