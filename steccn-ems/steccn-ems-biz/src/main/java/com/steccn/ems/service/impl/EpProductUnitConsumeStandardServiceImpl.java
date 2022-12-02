package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.ems.domain.EpProductUnitConsumeStandard;
import com.steccn.ems.dto.EpProductUnitStandardDTO;
import com.steccn.ems.dto.EpProductUnitStandardPageDTO;
import com.steccn.ems.service.EpProductUnitConsumeStandardService;
import com.steccn.ems.mapper.EpProductUnitConsumeStandardMapper;
import com.steccn.ems.vo.EpProductUnitConsumeStandardVO;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
* @author yequan
* @description 针对表【ep_product_unit_consume_standard(产品单耗标准)】的数据库操作Service实现
* @createDate 2022-11-29 19:27:28
*/
@Service
public class EpProductUnitConsumeStandardServiceImpl extends ServiceImpl<EpProductUnitConsumeStandardMapper, EpProductUnitConsumeStandard>
    implements EpProductUnitConsumeStandardService{

    @Override
    public List<EpProductUnitConsumeStandardVO> getEpProductUnitConsumeStandardList(EpProductUnitStandardPageDTO dto) {
        EpProductUnitConsumeStandardVO epProductUnitStandardVO = new EpProductUnitConsumeStandardVO();
        List<EpProductUnitConsumeStandardVO> epProductUnitStandardVOS = Lists.newArrayList();
        epProductUnitStandardVOS.add(epProductUnitStandardVO);
        return epProductUnitStandardVOS;
    }


    @Override
    public Boolean addOrEdit(EpProductUnitStandardDTO dto) {
        //去数据库中根据名称查询产品单耗个数
        long size = count(new LambdaQueryWrapper<EpProductUnitConsumeStandard>()
                .eq(EpProductUnitConsumeStandard::getName, dto.getName()));
        Assert.isTrue(size ==0, "产品单耗的名称不能重复");
        //创建domain实体类对象
        EpProductUnitConsumeStandard unitConsumeStandard = new EpProductUnitConsumeStandard();
        //复制传进来的dto
        BeanUtils.copyProperties(dto,unitConsumeStandard);
        long sum = count();
        String code = "BZ"+String.format("%06d", sum);
        //设置标准编码
        unitConsumeStandard.setCode(code);
        //检测有效期是否有重叠部分,如果有提示“与现有标准【#现有标准的名称#】有效期有重叠”，无法保存
        if (Objects.nonNull(dto.getStandardType())
                &&Objects.nonNull(dto.getEnergyType())
                &&Objects.nonNull(dto.getProductType())
                &&Objects.nonNull(dto.getProductThick())){
           List<EpProductUnitConsumeStandard> dataList= list(new LambdaQueryWrapper<EpProductUnitConsumeStandard>()
                    .eq(EpProductUnitConsumeStandard::getStandardType, dto.getStandardType())
                    .eq(EpProductUnitConsumeStandard::getEnergyType, dto.getEnergyType())
                    .eq(EpProductUnitConsumeStandard::getProductType, dto.getProductType())
                    .eq(EpProductUnitConsumeStandard::getProductThick, dto.getProductThick())
                    //老的开始时间小于等于dto的有效期结束时间,表明时间有重叠
                    .lt(EpProductUnitConsumeStandard::getValidStart,dto.getValidEnd())
                    //老的结束时间小于等于dto的有效开始时间,表明时间有重叠
                    .gt(EpProductUnitConsumeStandard::getValidEnd,dto.getValidStart()));
            String names = new String();
            if (CollUtil.isNotEmpty(dataList)){
                for (EpProductUnitConsumeStandard data : dataList) {
                    names = data.getName()+" ";
                }
                throw new BusinessException("与现有标准"+names+"有效期有重叠");
            }


        }

        if (Objects.isNull(dto.getId())){
            save(unitConsumeStandard);
        }else {
            updateById(unitConsumeStandard);
        }
        return Boolean.TRUE;
    }
}




