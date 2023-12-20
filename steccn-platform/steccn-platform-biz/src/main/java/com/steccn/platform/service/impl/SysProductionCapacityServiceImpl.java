package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.platform.domain.SysProductionCapacity;
import com.steccn.platform.dto.SysProductionCapacityDTO;
import com.steccn.platform.service.SysProductionCapacityService;
import com.steccn.platform.mapper.SysProductionCapacityMapper;
import com.steccn.platform.vo.SysProductionCapacityVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author yequan
 * @description 针对表【sys_production_capacity(标准产能表)】的数据库操作Service实现
 * @createDate 2023-12-19 16:50:56
 */
@Service
public class SysProductionCapacityServiceImpl extends ServiceImpl<SysProductionCapacityMapper, SysProductionCapacity>
        implements SysProductionCapacityService {

    @Override
    public Page<SysProductionCapacityVO> capacityPage(SysProductionCapacityDTO dto) {
        //获取日期
        LocalDate start = null, end = null;
        if (StrUtil.isNotBlank(dto.getMonthStr())) {
            String startStr = dto.getMonthStr() + "-01";
            start = LocalDate.parse(startStr);
            end = start.plusMonths(1);
        }
        //遍历数据
        Page<SysProductionCapacity> pageData = page(new Page<>(dto.getCurrent(), dto.getSize()),
                new LambdaQueryWrapper<SysProductionCapacity>()
                        .ge(Objects.nonNull(start), SysProductionCapacity::getClassDate, start)
                        .lt(Objects.nonNull(end), SysProductionCapacity::getClassDate, end));
        Page<SysProductionCapacityVO> resPage = new Page<>();
        BeanUtil.copyProperties(pageData, resPage);
        if (CollUtil.isNotEmpty(pageData.getRecords())) {
            List<SysProductionCapacityVO> resList = BeanUtil.copyToList(pageData.getRecords(), SysProductionCapacityVO.class);
            resPage.setRecords(resList);
        }
        return resPage;
    }


}




