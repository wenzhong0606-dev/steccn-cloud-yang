package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.api.config.filter.IFilterConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.pms.domain.PmProcess;
import com.steccn.pms.domain.PmsWorkOrderBom;
import com.steccn.pms.domain.PmsWorkOrderCraftStation;
import com.steccn.pms.dto.BomStationDTO;
import com.steccn.pms.dto.PmsWorkOrderBomDTO;
import com.steccn.pms.dto.PmsWorkOrderCraftStationDTO;
import com.steccn.pms.service.PmProcessService;
import com.steccn.pms.service.PmsWorkOrderBomService;
import com.steccn.pms.mapper.PmsWorkOrderBomMapper;
import com.steccn.pms.vo.PmProcessVO;
import com.steccn.pms.vo.PmsProduceOrderCraftVO;
import com.steccn.pms.vo.PmsWorkOrderBomVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_work_order_bom(生产工单-物料清单信息)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
@RequiredArgsConstructor
public class PmsWorkOrderBomServiceImpl extends ServiceImpl<PmsWorkOrderBomMapper, PmsWorkOrderBom>
        implements PmsWorkOrderBomService {

    private final RemoteDictFeignApi remoteDictFeignApi;
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final PmProcessService processService;

    /**
     * 保存物料信息
     *
     * @param materialList
     * @param id
     */
    @Override
    public void saveMaterial(List<PmsWorkOrderBomDTO> materialList, Long id) {
        //1.定义数据集合
        List<PmsWorkOrderBom> bomList = Lists.newArrayList();
        //2.处理数据
        materialList.forEach(material -> {
            PmsWorkOrderBom pmsWorkOrderBom = new PmsWorkOrderBom();
            BeanUtil.copyProperties(material, pmsWorkOrderBom);
            pmsWorkOrderBom.setWorkProduceId(id);
            bomList.add(pmsWorkOrderBom);
        });
        //3.批量保存
        saveBatch(bomList);
    }

    /**
     * 根据生产订单id查询物料信息
     *
     * @param id
     * @return
     */
    @Override
    public List<PmsWorkOrderBomVO> getMaterialList(Long id) {
        //1.定义查询条件
        LambdaQueryWrapper<PmsWorkOrderBom> eq = new LambdaQueryWrapper<PmsWorkOrderBom>().eq(PmsWorkOrderBom::getWorkProduceId, id);
        //2.获取数据
        List<PmsWorkOrderBom> list = list(eq);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        //3.处理数据
        Set<Long> ids = Sets.newHashSet();
        List<Long> materialIds = Lists.newArrayList();
        List<Long> processIds = Lists.newArrayList();
        list.forEach(pmsWorkOrderBom -> {
            ids.add(pmsWorkOrderBom.getUnitId());
            materialIds.add(pmsWorkOrderBom.getModuleId());
            processIds.add(pmsWorkOrderBom.getProcessId());
        });
        //3.1.获取字典数据
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(ids)).get();
        //3.2 获取物料数据
        Map<Long, EqpSpareVO> spareVOMap = FeignOptional.of(remoteSpareFeignApi.listById(materialIds)).get();
        //3.3 获取工序数据
        Map<Long, PmProcessVO> processVOMap = processService.listById(processIds);
        List<PmsWorkOrderBomVO> pmsWorkOrderBomVOList = Lists.newArrayList();
        list.forEach(pmsWorkOrderBom -> {
            PmsWorkOrderBomVO pmsWorkOrderBomVO = new PmsWorkOrderBomVO();
            BeanUtil.copyProperties(pmsWorkOrderBom, pmsWorkOrderBomVO);
            EqpSpareVO eqpSpareVO = spareVOMap.get(pmsWorkOrderBomVO.getModuleId());
            if (Objects.nonNull(eqpSpareVO)) {
                pmsWorkOrderBomVO.setName(eqpSpareVO.getName());
                pmsWorkOrderBomVO.setCode(eqpSpareVO.getCode());
            }
            //处理工序数据
            if (CollUtil.isNotEmpty(processVOMap)) {
                pmsWorkOrderBomVO.setProcessName(processVOMap.get(pmsWorkOrderBomVO.getProcessId()).getName());
            }
            pmsWorkOrderBomVO.setUnit(dictMap.get(pmsWorkOrderBomVO.getUnitId()));
            pmsWorkOrderBomVOList.add(pmsWorkOrderBomVO);
        });
        return pmsWorkOrderBomVOList;
    }

    @Override
    public Boolean delete(Integer id) {
        return removeById(id);
    }

    @Override
    public Map<Long, List<PmsWorkOrderBomVO>> materials(Set<Long> craftSet) {
        Map<Long, List<PmsWorkOrderBomVO>> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(craftSet)) {
            List<PmsWorkOrderBom> list = list(new LambdaQueryWrapper<PmsWorkOrderBom>().in(PmsWorkOrderBom::getWorkProduceId, craftSet));
            if (CollUtil.isNotEmpty(list)) {
                List<PmsWorkOrderBomVO> craftVOList = BeanUtil.copyToList(list, PmsWorkOrderBomVO.class);
                resMap = craftVOList.parallelStream().collect(Collectors.groupingBy(PmsWorkOrderBomVO::getWorkProduceId));
                //处理字典工序数据
                Set<Long> ids = Sets.newHashSet();
                List<Long> materialIds = Lists.newArrayList();
                List<Long> processIds = Lists.newArrayList();
                resMap.forEach((key, value) -> {
                    value.forEach(
                            material -> {
                                ids.add(material.getUnitId());
                                materialIds.add(material.getModuleId());
                                processIds.add(material.getProcessId());
                            }
                    );
                });
                //3.1.获取字典数据
                Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(ids)).get();
                //3.2 获取物料数据
                Map<Long, EqpSpareVO> spareVOMap = FeignOptional.of(remoteSpareFeignApi.listById(materialIds)).get();
                EqpSpareVO spareVO = new EqpSpareVO();
                //3.3 获取工序数据
                Map<Long, PmProcessVO> processVOMap = processService.listById(processIds);
                PmProcessVO pmProcessVO = new PmProcessVO();
                resMap.forEach((key, value) -> {
                    value.forEach(
                            material -> {
                                //处理物料数据
                                EqpSpareVO eqpSpareVO = spareVOMap.getOrDefault(material.getModuleId(), spareVO);
                                material.setName(eqpSpareVO.getName());
                                material.setCode(eqpSpareVO.getCode());
                                //处理工序数据
                                PmProcessVO processVO = processVOMap.getOrDefault(material.getProcessId(), pmProcessVO);
                                material.setProcessName(processVO.getName());
                                material.setUnit(dictMap.get(material.getUnitId()));
                            }
                    );
                });

            }
        }
        return resMap;
    }

    @Override
    public Boolean deleteBom(Set<Long> idSet, Long id) {
        if (CollUtil.isNotEmpty(idSet) && Objects.nonNull(id)) {
            return remove(new LambdaQueryWrapper<PmsWorkOrderBom>().notIn(PmsWorkOrderBom::getId, idSet).eq(PmsWorkOrderBom::getWorkProduceId, id));
        }
        return Boolean.TRUE;
    }

    @Override
    public Map<Long, List<BaseSelectVO<Long>>> getProcessMaterial(Set<Long> produceOrderIdSet, Set<Long> processIdSet) {
        Map<Long, List<BaseSelectVO<Long>>> resMap = Maps.newHashMap();
        List<PmsWorkOrderBom> dbList = list(new LambdaQueryWrapper<PmsWorkOrderBom>()
                .in(PmsWorkOrderBom::getWorkProduceId, produceOrderIdSet)
                .in(PmsWorkOrderBom::getProcessId, processIdSet));
        if (CollUtil.isNotEmpty(dbList)) {
            //获取子物料的集合
            Set<Long> moduleSet = dbList.stream().map(PmsWorkOrderBom::getModuleId).collect(Collectors.toSet());
            //获取物料数据
            Map<Long, String> spareVOMap = FeignOptional.of(remoteSpareFeignApi.listByIdCh(moduleSet)).get();

            Map<Long, List<PmsWorkOrderBom>> dbMap = dbList.stream().collect(Collectors.groupingBy(PmsWorkOrderBom::getProcessId));
            dbMap.forEach((k, v) -> {
                List<BaseSelectVO<Long>> resList = Lists.newArrayList();
                for (PmsWorkOrderBom pmsWorkOrderBom : v) {
                    resList.add(new BaseSelectVO<>(pmsWorkOrderBom.getModuleId(), spareVOMap.get(pmsWorkOrderBom.getModuleId())));
                }
                resMap.put(k, resList);
            });
        }
        return resMap;
    }

    @Override
    public Map<Long, Set<Long>> materialOrderStation(List<TupleDTO<Long, Long>> conditionList) {
        Map<Long, Set<Long>> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(conditionList)) {
            List<TupleDTO<Long, String>> dataList = this.baseMapper.materialOrderStation(conditionList);
            if (CollUtil.isNotEmpty(dataList)) {
                dataList.forEach(ele -> {
                    String field2 = ele.getField2();
                    if (StrUtil.isNotBlank(field2)) {
                        String[] split = field2.split(",");
                        Set<Long> resSet = Sets.newHashSet();
                        try {
                            for (String s : split) {
                                resSet.add(Long.parseLong(s));
                            }
                        } catch (Exception e) {
                            log.error(e.getLocalizedMessage());
                        }
                        resMap.put(ele.getField1(), resSet);
                    }
                });
            }
        }
        return resMap;
    }

    @Override
    public List<BomStationDTO> listStation(Set<Long> bomIds) {
        //1.自定义sql获取工位信息
        return this.baseMapper.listStation(bomIds);
    }
}




