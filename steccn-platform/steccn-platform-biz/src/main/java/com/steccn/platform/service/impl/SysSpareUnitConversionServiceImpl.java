package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.platform.domain.SysSpareUnitConversion;
import com.steccn.platform.service.SysSpareUnitConversionService;
import com.steccn.platform.mapper.SysSpareUnitConversionMapper;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【sys_spare_unit_conversion(备件单位换算)】的数据库操作Service实现
 * @createDate 2022-11-11 20:45:51
 */
@Service
public class SysSpareUnitConversionServiceImpl extends ServiceImpl<SysSpareUnitConversionMapper, SysSpareUnitConversion>
        implements SysSpareUnitConversionService {

    @Override
    public Map<Long, List<SysSpareUnitConversionVO>> convert(Set<Long> spareSet) {
        //定义map
        Map<Long, List<SysSpareUnitConversionVO>> resMap = Maps.newHashMap();
        //如果集合不为空
        if (CollUtil.isNotEmpty(spareSet)) {
            //查询物料集合
            List<SysSpareUnitConversion> dataList = list(new LambdaQueryWrapper<SysSpareUnitConversion>()
                    .in(SysSpareUnitConversion::getSpareId, spareSet));
            //物料转换单位不为空
            if (CollUtil.isNotEmpty(dataList)) {
                List<SysSpareUnitConversionVO> voList = BeanUtil.copyToList(dataList, SysSpareUnitConversionVO.class);
                resMap = voList.stream().collect(Collectors.groupingBy(SysSpareUnitConversionVO::getSpareId));
            }
        }
        return resMap;
    }
}




