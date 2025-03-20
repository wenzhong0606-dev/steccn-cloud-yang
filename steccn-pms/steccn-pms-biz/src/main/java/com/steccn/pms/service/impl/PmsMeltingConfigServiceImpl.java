package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmsMeltingConfig;
import com.steccn.pms.domain.PmsPullForce;
import com.steccn.pms.dto.PmsMeltingConfigDTO;
import com.steccn.pms.service.PmsMeltingConfigService;
import com.steccn.pms.mapper.PmsMeltingConfigMapper;
import com.steccn.pms.vo.PmsMeltingConfigVO;
import com.steccn.pms.vo.PmsPullForceVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.format.DateTimeFormatter.ISO_DATE;

/**
* @author Admin
* @description 针对表【pms_melting_config】的数据库操作Service实现
* @createDate 2025-03-20 15:00:05
*/
@Service
public class PmsMeltingConfigServiceImpl extends ServiceImpl<PmsMeltingConfigMapper, PmsMeltingConfig>
    implements PmsMeltingConfigService{

    @Override
    public Boolean add(PmsMeltingConfigDTO dto) {
        //先删除当天数据
        remove(new LambdaQueryWrapper<PmsMeltingConfig>()
                .eq(PmsMeltingConfig::getMeltingDate,dto.getMeltingDate()));
        PmsMeltingConfig model = new PmsMeltingConfig();
        BeanUtil.copyProperties(dto,model);
        save(model);
        return true;
    }

    @Override
    public List<PmsMeltingConfigVO> list(String monthDate) {
        //切日期
        LocalDate date = LocalDate.parse(monthDate + "-01", ISO_DATE);
        LocalDate startDate = date.minusWeeks(1);
        LocalDate endDate = date.plusMonths(1).plusWeeks(1);
        //获取列表
        List<PmsMeltingConfig> pmsMeltingConfigList = list(new LambdaQueryWrapper<PmsMeltingConfig>().ge(PmsMeltingConfig::getMeltingDate, startDate).le(PmsMeltingConfig::getMeltingDate, endDate));
        List<LocalDate> localDateList = LocalDateUtil.getLocalDateByStartEndDate(startDate, endDate);
        List<PmsMeltingConfigVO> pullForceVOList = new ArrayList<>();
        for (LocalDate localDate : localDateList) {
            PmsMeltingConfigVO pmsMeltingConfigVO = new PmsMeltingConfigVO();
            Optional<PmsMeltingConfig> present = pmsMeltingConfigList.stream().filter(v -> v.getMeltingDate().equals(localDate)).findAny();
            if (present.isPresent()) {
                BeanUtils.copyProperties(present.get(), pmsMeltingConfigVO);
            } else {
                pmsMeltingConfigVO.setMeltingDate(localDate);
            }
            pullForceVOList.add(pmsMeltingConfigVO);
        }
        return pullForceVOList;
    }

    @Override
    public Boolean addMeltingRate() {
        LocalDate startDate = LocalDate.of(2022,1,1);
        LocalDate endDate = LocalDate.now();
        List<LocalDate> localDateList = LocalDateUtil.getLocalDateByStartEndDate(startDate, endDate);
        for (LocalDate localDate : localDateList) {
            PmsMeltingConfigDTO dto = new PmsMeltingConfigDTO();
            dto.setMeltingDate(localDate);
            dto.setMeltingNum(new BigDecimal(0.8));
            add(dto);
        }
        return true;
    }
}




