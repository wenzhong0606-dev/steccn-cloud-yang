package com.steccn.oa.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import com.steccn.common.util.ShardTimeUtil;
import com.steccn.common.vo.BaseValueNameVO;
import com.steccn.common.vo.BaseValueVO;
import com.steccn.common.vo.CommonBarLineVO;
import com.steccn.ems.vo.PmsOperateEnergyVO;
import com.steccn.oa.domain.HrmEmpCount;
import com.steccn.oa.domain.Hrmresource;
import com.steccn.oa.dto.OaDateDTO;
import com.steccn.oa.mapper.HrmresourceMapper;
import com.steccn.oa.service.HrmEmpCountService;
import com.steccn.oa.mapper.HrmEmpCountMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
* @author Admin
* @description 针对表【hrm_emp_count】的数据库操作Service实现
* @createDate 2024-11-25 13:21:36
*/
@Service
@RequiredArgsConstructor
public class HrmEmpCountServiceImpl extends ServiceImpl<HrmEmpCountMapper, HrmEmpCount>
    implements HrmEmpCountService{

    private final HrmresourceMapper hrmresourceMapper;

    @Override
    public PmsOperateEnergyVO countDash(OaDateDTO dto) {
        PmsOperateEnergyVO result = new PmsOperateEnergyVO();
        List<HrmEmpCount> data = list(new LambdaQueryWrapper<HrmEmpCount>()
                .between(HrmEmpCount::getMonth, dto.getStart(), dto.getEnd()));
        Assert.notEmpty(data, "数据为空");
        //当前人数
        Hrmresource all = hrmresourceMapper.selectOne(new QueryWrapper<Hrmresource>().select("Count(*) As locationid")
                .lambda()
                .lt(Hrmresource::getCreated, LocalDate.now().atTime(0,0,0)));
        Hrmresource leaveJob = hrmresourceMapper.selectOne(new QueryWrapper<Hrmresource>().select("Count(*) As locationid")
                .lambda()
                .lt(Hrmresource::getModified, LocalDate.now().atTime(0,0,0))
                .in(Hrmresource::getStatus, Sets.newHashSet(5,4)));
        List<LocalDate> dateList = ShardTimeUtil.cutDate("M", dto.getStart(), dto.getEnd());
        CommonBarLineVO commonBarLineVO = new CommonBarLineVO();
        List<String> xAisList = Lists.newArrayList();
        List<BigDecimal> valueList = Lists.newArrayList();
        LocalDate now = LocalDate.now();
        for(LocalDate tmp:dateList){
            if(now.getYear()==tmp.getYear()&&tmp.getMonthValue() > now.getMonthValue()){
                continue;
            }
            xAisList.add(tmp.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            if(now.getYear()==tmp.getYear()&&tmp.getMonthValue()==now.getMonthValue()){
                valueList.add(new BigDecimal(all.getLocationid()-leaveJob.getLocationid()));
                continue;
            }
            HrmEmpCount hrmEmpCount = data.stream().filter(v -> v.getMonth().equals(tmp)).findFirst().orElse(null);
            if(Objects.isNull(hrmEmpCount)){
                valueList.add(BigDecimal.ZERO);
            }else {
                valueList.add(new BigDecimal(hrmEmpCount.getOnJob()));
            }
        }
        commonBarLineVO.setXAxis(new BaseValueVO(xAisList));
        commonBarLineVO.setLegend(new BaseValueVO(Arrays.asList("在职人数")));
        commonBarLineVO.setSeries(Arrays.asList(new BaseValueNameVO<>(valueList, "在职人数")));
        result.setCommonBarLineVO(commonBarLineVO);
        result.setTotalQuantity(new BigDecimal(all.getLocationid()-leaveJob.getLocationid()));
        return result;
    }
}




