package com.steccn.platform.service;

import com.steccn.platform.domain.SysSpareUnitConversion;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.vo.SysSpareUnitConversionVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author yequan
* @description 针对表【sys_spare_unit_conversion(备件单位换算)】的数据库操作Service
* @createDate 2022-11-11 20:45:51
*/
public interface SysSpareUnitConversionService extends IService<SysSpareUnitConversion> {

    /**
     * 物料id的单位转换
     * @param spareSet
     * @return
     */
    Map<Long, List<SysSpareUnitConversionVO>> convert(Set<Long> spareSet);
}
