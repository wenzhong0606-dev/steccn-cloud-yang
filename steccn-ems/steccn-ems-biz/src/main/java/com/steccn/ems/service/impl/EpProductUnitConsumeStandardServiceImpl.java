package com.steccn.ems.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.ems.domain.EpProductUnitConsumeStandard;
import com.steccn.ems.dto.EpProductUnitStandardDTO;
import com.steccn.ems.service.EpProductUnitConsumeStandardService;
import com.steccn.ems.mapper.EpProductUnitConsumeStandardMapper;
import com.steccn.ems.vo.EpProductUnitConsumeStandardVO;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author yequan
* @description 针对表【ep_product_unit_consume_standard(产品单耗标准)】的数据库操作Service实现
* @createDate 2022-11-29 19:27:28
*/
@Service
public class EpProductUnitConsumeStandardServiceImpl extends ServiceImpl<EpProductUnitConsumeStandardMapper, EpProductUnitConsumeStandard>
    implements EpProductUnitConsumeStandardService{

    @Override
    public List<EpProductUnitConsumeStandardVO> getEpProductUnitConsumeStandardList() {
        EpProductUnitConsumeStandardVO epProductUnitStandardVO = new EpProductUnitConsumeStandardVO();
        List<EpProductUnitConsumeStandardVO> epProductUnitStandardVOS = Lists.newArrayList();
        epProductUnitStandardVOS.add(epProductUnitStandardVO);
        return epProductUnitStandardVOS;
    }


    @Override
    public Boolean addOrEdit(EpProductUnitStandardDTO dto) {
        //去数据库中
        long size = count(new LambdaQueryWrapper<EpProductUnitConsumeStandard>()
                .eq(EpProductUnitConsumeStandard::getName, dto.getName()));
        Assert.isTrue(size !=0, "产品单耗的名称不能重复");
        EpProductUnitConsumeStandard unitConsumeStandard = new EpProductUnitConsumeStandard();
        BeanUtils.copyProperties(dto,unitConsumeStandard);
        long sum = count();
        String code = "BZ"+String.format("%06d", sum);
        unitConsumeStandard.setCode(code);

        return null;
    }
}




