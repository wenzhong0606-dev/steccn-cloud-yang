package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpEnergyType;
import com.steccn.ems.dto.EpAddEnergyTypeDTO;
import com.steccn.ems.service.EpEnergyTypeService;
import com.steccn.ems.mapper.EpEnergyTypeMapper;
import com.steccn.ems.vo.EpAddEnergyTypeVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author yequan
* @description 针对表【ep_energy_type(能源类型表)】的数据库操作Service实现
* @createDate 2022-11-29 19:27:28
*/
@Service
@RequiredArgsConstructor
@Slf4j
public class EpEnergyTypeServiceImpl extends ServiceImpl<EpEnergyTypeMapper, EpEnergyType>
    implements EpEnergyTypeService{

    private final RemoteDictFeignApi remoteDictFeignApi;
    /**
     * 新增能源类型
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer add(EpAddEnergyTypeDTO dto) {
        List<EpEnergyType> epEnergyTypes = list(new LambdaQueryWrapper<EpEnergyType>().eq(EpEnergyType::getName, dto.getName()));
        if (epEnergyTypes.size()>1){
            throw new BusinessException("能源类型的名称不能重复!");
        }
        EpEnergyType epEnergyType = new EpEnergyType();
        BeanUtils.copyProperties(dto,epEnergyType);
        save(epEnergyType);
        return epEnergyType.getId();
    }

    @Override
    public EpAddEnergyTypeVO getEpAddEnergyTypeVO(Integer id) {
        return null;
    }

    @Override
    public List<EpAddEnergyTypeVO> getEpAddEnergyTypeVOList() {
        //查询所有
        List<EpEnergyType> epEnergyTypes = list(new LambdaQueryWrapper<EpEnergyType>());
        List<EpAddEnergyTypeVO> epAddEnergyTypeVOS= Lists.newArrayList();
        if (CollUtil.isEmpty(epEnergyTypes)){
            return epAddEnergyTypeVOS;
        }
        epEnergyTypes.forEach(s->{
           Set<Long> measure = Sets.newHashSet();
           measure.add(s.getMeasureUnit());
           Map<Long, String> longStringMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(measure)).get();
            String measureName = longStringMap.get(s.getMeasureUnit());
            EpAddEnergyTypeVO epAddEnergyTypeVO = new EpAddEnergyTypeVO();
            BeanUtils.copyProperties(s,epAddEnergyTypeVO);
            //设置单位名称
            epAddEnergyTypeVO.setMeasureUnit(measureName);
            List<BaseSelectVO<Integer>> linkTableString = Lists.newArrayList();
            //获取关联表
            List<Integer> linkTable = s.getLinkTable();
           if (CollUtil.isNotEmpty(linkTable)){
               linkTable.forEach(q->{
                   BaseSelectVO<Integer> integerBaseSelectVO = new BaseSelectVO<>();
                   EpManagerConstant.EpEnergyTypes energyTypes = EpManagerConstant.EpEnergyTypes.getById(q);
                   integerBaseSelectVO.setId(q);
                   integerBaseSelectVO.setName(energyTypes.getName());
                   linkTableString.add(integerBaseSelectVO);
               });
           }
            epAddEnergyTypeVO.setLinkTable(linkTableString);
            epAddEnergyTypeVOS.add(epAddEnergyTypeVO);
        });
        return epAddEnergyTypeVOS;
    }

}




