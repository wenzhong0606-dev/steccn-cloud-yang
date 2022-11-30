package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpEnergyType;
import com.steccn.ems.dto.EpEnergyTypeDTO;
import com.steccn.ems.service.EpEnergyTypeService;
import com.steccn.ems.mapper.EpEnergyTypeMapper;
import com.steccn.ems.vo.EpEnergyTypeVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        implements EpEnergyTypeService {

    private final RemoteDictFeignApi remoteDictFeignApi;

    /**
     * 新增能源类型
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addOrEdit(EpEnergyTypeDTO dto) {
        long size = count(new LambdaQueryWrapper<EpEnergyType>()
                .eq(EpEnergyType::getName, dto.getName())
                .eq(EpEnergyType::getShowFlag, MpConstant.YES));
        Assert.isTrue(size != 0, "能源类型的名称不能重复");
        EpEnergyType epEnergyType = new EpEnergyType();
        BeanUtils.copyProperties(dto, epEnergyType);
        if (Objects.isNull(dto.getId())) {
            save(epEnergyType);
        } else {
            if (dto.getId() < 6) {
                throw new BusinessException("前五项已写死,无法修改");
            }
            updateById(epEnergyType);
        }
        return Boolean.TRUE;
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    @Override
    public EpEnergyTypeVO getEpAddEnergyTypeVO(Integer id) {
        EpEnergyType epEnergyType = getById(id);
        if (Objects.isNull(epEnergyType)) {
            return null;
        }
        EpEnergyTypeVO epEnergyTypeVO = new EpEnergyTypeVO();
        BeanUtils.copyProperties(epEnergyType, epEnergyTypeVO);
        //设置单位名称
        if (Objects.nonNull(epEnergyType.getMeasureUnit())) {
            Set<Long> measure = Sets.newHashSet();
            measure.add(epEnergyType.getMeasureUnit());
            Map<Long, String> longStringMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(measure)).get();
            String measureName = longStringMap.get(epEnergyType.getMeasureUnit());
            epEnergyTypeVO.setMeasureUnitName(measureName);
        }
        //关联图表
        List<BaseSelectVO<Integer>> linkTableList  = energyType(epEnergyType.getLinkTable());
        epEnergyTypeVO.setLinkTable(linkTableList);
        return epEnergyTypeVO;
    }

    /**
     *
     * @param linkTable
     * @return
     */
    private List<BaseSelectVO<Integer>> energyType(List<Integer> linkTable) {
        //获取关联表
        List<BaseSelectVO<Integer>> linkTableString = Lists.newArrayList();
        if (CollUtil.isNotEmpty(linkTable)) {
            linkTable.forEach(q -> {
                BaseSelectVO<Integer> baseSelectVo = new BaseSelectVO<>();
                EpManagerConstant.EpEnergyTypes energyTypes = EpManagerConstant.EpEnergyTypes.getById(q);
                baseSelectVo.setId(q);
                baseSelectVo.setName(energyTypes.getName());
                linkTableString.add(baseSelectVo);
            });
        }
        return linkTableString;
    }

    @Override
    public List<EpEnergyTypeVO> getEpEnergyTypeVOList() {
        //查询所有
        List<EpEnergyType> epEnergyTypes = list(new LambdaQueryWrapper<EpEnergyType>());
        List<EpEnergyTypeVO> epAddEnergyTypeVOS = Lists.newArrayList();
        if (CollUtil.isEmpty(epEnergyTypes)) {
            return epAddEnergyTypeVOS;
        }
        epEnergyTypes.forEach(s -> {
            EpEnergyTypeVO epAddEnergyTypeVO = new EpEnergyTypeVO();
            BeanUtils.copyProperties(s, epAddEnergyTypeVO);
            //设置单位名称
            if (Objects.nonNull(s.getMeasureUnit())) {
                Set<Long> measure = Sets.newHashSet();
                measure.add(s.getMeasureUnit());
                Map<Long, String> longStringMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(measure)).get();
                String measureName = longStringMap.get(s.getMeasureUnit());
                epAddEnergyTypeVO.setMeasureUnitName(measureName);
            }
            //获取关联表
            List<BaseSelectVO<Integer>> linkTableList  = energyType(s.getLinkTable());
            epAddEnergyTypeVO.setLinkTable(linkTableList);
            epAddEnergyTypeVOS.add(epAddEnergyTypeVO);
        });
        return epAddEnergyTypeVOS;
    }

    @Override
    public Boolean delete(Integer id) {
        return null;
    }

}




