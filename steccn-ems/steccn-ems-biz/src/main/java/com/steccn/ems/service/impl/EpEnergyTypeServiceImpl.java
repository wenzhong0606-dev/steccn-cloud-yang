package com.steccn.ems.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpEnergyType;
import com.steccn.ems.domain.EpInstrumentAccount;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.dto.EpEnergyTypeDTO;
import com.steccn.ems.service.EpEnergyTypeService;
import com.steccn.ems.mapper.EpEnergyTypeMapper;
import com.steccn.ems.service.EpInstrumentAccountService;
import com.steccn.ems.service.EpPartitionService;
import com.steccn.ems.service.EpProductUnitConsumeStandardService;
import com.steccn.ems.vo.EpEnergyTypeVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【ep_energy_type(能源类型表)】的数据库操作Service实现
 * @createDate 2022-11-29 19:27:28
 */
@Service
@Slf4j
public class EpEnergyTypeServiceImpl extends ServiceImpl<EpEnergyTypeMapper, EpEnergyType>
        implements EpEnergyTypeService {
    @Autowired
    private RemoteDictFeignApi remoteDictFeignApi;
    @Autowired
    private EpPartitionService partitionService;
    @Autowired
    private EpInstrumentAccountService instrumentAccountService;

    /**
     * 默认能源,不可修改列表
     */
    private final Set<String> DEFAULT_ENERGY_SET = Sets.newHashSet("电", "水", "天然气", "燃料油", "燃料(气+油)");

    /**
     * 新增或编辑能源类型
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
        if (Objects.isNull(dto.getId())){
            Assert.isTrue(size ==0 , "能源类型的名称不能重复");
        }else {
            Assert.isTrue(size <=1 , "能源类型的名称不能重复");
        }
        long sizeMeasureMeterName = count(new LambdaQueryWrapper<EpEnergyType>()
                .eq(EpEnergyType::getMeasureMeterName, dto.getMeasureMeterName())
                .eq(EpEnergyType::getShowFlag, MpConstant.YES));
        if (Objects.isNull(dto.getId())){
            Assert.isTrue(sizeMeasureMeterName ==0 , "计量仪表名称不能重复");
        }else {
            Assert.isTrue(sizeMeasureMeterName <=1 , "计量仪表名称不能重复");
        }
        EpEnergyType epEnergyType = new EpEnergyType();
        BeanUtils.copyProperties(dto, epEnergyType);
        //link table是否合法
        Assert.isTrue(EpManagerConstant.EpEnergyTypes.codeSet().containsAll(dto.getLinkTable()),"关联图表参数异常");
        if (Objects.isNull(dto.getId())) {
            save(epEnergyType);
        } else {
            EpEnergyType oldEnergy = getById(dto.getId());
            Assert.notNull(oldEnergy,"编辑的能源类型不存在");
            Assert.isTrue(!DEFAULT_ENERGY_SET.contains(oldEnergy.getName()), "前五项已写死,无法修改");
            if (MpConstant.NO==epEnergyType.getShowFlag()){
                throw new BusinessException("该能源类型已删除无法编辑");
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
        Assert.isTrue(MpConstant.YES==epEnergyType.getShowFlag(),"该能源类型已删除");
        EpEnergyTypeVO epEnergyTypeVO = new EpEnergyTypeVO();
        BeanUtils.copyProperties(epEnergyType, epEnergyTypeVO);
        //设置单位名称
        if (Objects.nonNull(epEnergyType.getMeasureUnit())) {
            Map<Long, String> idNameMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(Sets.newHashSet(epEnergyType.getMeasureUnit()))).get();
            epEnergyTypeVO.setMeasureUnitName(idNameMap.get(epEnergyType.getMeasureUnit()));
        }
        //关联图表
        List<BaseSelectVO<Integer>> linkTableList  = energyTypeLinkTable(epEnergyType.getLinkTable());
        epEnergyTypeVO.setLinkTable(linkTableList);
        return epEnergyTypeVO;
    }

    /**
     *
     * @param linkTable
     * @return
     */
    private List<BaseSelectVO<Integer>> energyTypeLinkTable(List<Integer> linkTable) {
        //获取关联表
        List<BaseSelectVO<Integer>> linkTableList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(linkTable)) {
            linkTable.forEach(q -> {
                BaseSelectVO<Integer> baseSelectVo = new BaseSelectVO<>();
                EpManagerConstant.EpEnergyTypes energyTypes = EpManagerConstant.EpEnergyTypes.getByCode(q);
                Assert.notNull(energyTypes, "关联图表不存在");
                baseSelectVo.setId(q);
                baseSelectVo.setName(energyTypes.getName());
                linkTableList.add(baseSelectVo);
            });
        }
        return linkTableList;
    }

    @Override
    public List<EpEnergyTypeVO> getEpEnergyTypeVOList() {
        //查询所有可见标识为1的
        List<EpEnergyType> epEnergyTypes = list(new LambdaQueryWrapper<EpEnergyType>()
                .eq(EpEnergyType::getShowFlag,MpConstant.YES));
        List<EpEnergyTypeVO> epAddEnergyTypeVOS = Lists.newArrayList();
        if (CollUtil.isEmpty(epEnergyTypes)) {
            return epAddEnergyTypeVOS;
        }
        Set<Long> measures = Sets.newHashSet();
        for (EpEnergyType epEnergyType : epEnergyTypes) {
            measures.add(epEnergyType.getMeasureUnit());
        }
        //获取所有的单位名称和编码的map集合
        Map<Long, String> idNameMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(Sets.newHashSet(measures))).get();
        epEnergyTypes.forEach(s -> {
            EpEnergyTypeVO epAddEnergyTypeVO = new EpEnergyTypeVO();
            BeanUtils.copyProperties(s, epAddEnergyTypeVO);
            //设置单位名称
            if (Objects.nonNull(s.getMeasureUnit())) {
                epAddEnergyTypeVO.setMeasureUnitName(idNameMap.get(s.getMeasureUnit()));
            }
            //获取关联表
            List<BaseSelectVO<Integer>> linkTableList  = energyTypeLinkTable(s.getLinkTable());
            epAddEnergyTypeVO.setLinkTable(linkTableList);
            epAddEnergyTypeVOS.add(epAddEnergyTypeVO);
        });
        return epAddEnergyTypeVOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Integer id) {
        EpEnergyType epEnergyType = getById(id);
        Assert.notNull(epEnergyType,"该id图表不存在,无法删除");
        Assert.isTrue(!DEFAULT_ENERGY_SET.contains(epEnergyType.getName()), "前五项已写死,无法删除");
        //删除能源分区树
        List<Long> partitionIds = partitionService.list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getType, id))
                .stream().map(EpPartition::getId).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(partitionIds)){
//            long count = instrumentAccountService.count(new LambdaQueryWrapper<EpInstrumentAccount>()
//                    .in(EpInstrumentAccount::getPartitionNode, partitionIds)
//                    .eq(EpInstrumentAccount::getShowFlag, MpConstant.YES));
//            Assert.isTrue(count==0,"该能源类型下有绑定仪表,无法删除");
            partitionService.removeByIds(partitionIds);
        }

        epEnergyType.setShowFlag(MpConstant.NO);
        updateById(epEnergyType);
        return Boolean.TRUE;
    }

    @Override
    public Map<Integer, EpEnergyTypeVO> getBylinkTable(Integer code) {
        //获取能源类型列表
        List<EpEnergyTypeVO> epEnergyTypeVOList = getEpEnergyTypeVOList();
        if (CollUtil.isEmpty(epEnergyTypeVOList)){
            throw new BusinessException("数据库无任何能源类型");
        }
        Map<Integer, EpEnergyTypeVO> idAndMeasures =Maps.newHashMap();
        epEnergyTypeVOList.stream().filter(s->Objects.nonNull(s)).forEach(s->{
            if (CollUtil.isNotEmpty(s.getLinkTable())){
                List<Integer> codes = s.getLinkTable().stream().map(BaseSelectVO::getId).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(codes)&&codes.contains(code)){
                    idAndMeasures.put(s.getId(),s);
                }
            }
        });
        return idAndMeasures;
    }

    @Override
    public Map<Integer, String> getBySetIds(Set<Integer> ids) {
        HashMap<Integer, String> resHasMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(ids)){
            List<EpEnergyType> epEnergyTypes = list(new LambdaQueryWrapper<EpEnergyType>().in(EpEnergyType::getId, ids));
            if (CollUtil.isNotEmpty(epEnergyTypes)){
                for (EpEnergyType epEnergyType : epEnergyTypes) {
                    resHasMap.put(epEnergyType.getId(),epEnergyType.getName());
                }
            }
        }
        return resHasMap;
    }

}




