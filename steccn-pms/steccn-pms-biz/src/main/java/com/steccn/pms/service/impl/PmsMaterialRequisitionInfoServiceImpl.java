package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.platform.feign.RemoteStationWarehouseFeignApi;
import com.steccn.platform.vo.SysStationWarehouseVO;
import com.steccn.pms.domain.PmsMaterialRequisitionInfo;
import com.steccn.pms.dto.BomStationDTO;
import com.steccn.pms.dto.MaterialRequisitionDTO;
import com.steccn.pms.mapper.PmsMaterialRequisitionInfoMapper;
import com.steccn.pms.service.PmsMaterialRequisitionInfoService;
import com.steccn.pms.vo.MaterialRequisitionInfoVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_material_requisition_info(生产领用单详情)】的数据库操作Service实现
 * @createDate 2022-11-02 10:10:08
 */
@Service
@RequiredArgsConstructor
public class PmsMaterialRequisitionInfoServiceImpl extends ServiceImpl<PmsMaterialRequisitionInfoMapper, PmsMaterialRequisitionInfo>
        implements PmsMaterialRequisitionInfoService {

    private final RemoteStationWarehouseFeignApi remoteStationWarehouseFeignApi;

    @Override
    public Boolean editList(MaterialRequisitionDTO materialRequisitionDTO) {
        //1.定义修改的数据集合和新增的数据集合
        List<PmsMaterialRequisitionInfo> list = Lists.newArrayList();
        Set<Long> idSet = Sets.newHashSet();
        //2.处理数据
        materialRequisitionDTO.getList().forEach(materialRequisitionInfoDTO -> {
            PmsMaterialRequisitionInfo materialRequisitionInfo = new PmsMaterialRequisitionInfo();
            BeanUtil.copyProperties(materialRequisitionInfoDTO, materialRequisitionInfo);
            materialRequisitionInfo.setMaterialRequisitionId(materialRequisitionDTO.getId());
            if (Objects.nonNull(materialRequisitionInfo.getId())) {
                list.add(materialRequisitionInfo);
                idSet.add(materialRequisitionInfo.getId());
            } else {
                list.add(materialRequisitionInfo);
            }
        });
        //3.删除其他多余的数据
        remove(new LambdaQueryWrapper<PmsMaterialRequisitionInfo>()
                .eq(PmsMaterialRequisitionInfo::getMaterialRequisitionId, materialRequisitionDTO.getId())
                .notIn(CollUtil.isNotEmpty(idSet), PmsMaterialRequisitionInfo::getId, idSet));
        //4.批量修改新增
        return saveOrUpdateBatch(list);
    }

    @Override
    public Map<Long, BigDecimal> getTotalQuantity(Set<Long> ids) {
        Map<Long, BigDecimal> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(ids)) {
            List<PmsMaterialRequisitionInfo> list = list(new QueryWrapper<PmsMaterialRequisitionInfo>().select("sum(quantity) as quantity,material_requisition_id").lambda()
                    .in(PmsMaterialRequisitionInfo::getMaterialRequisitionId, ids)
                    .groupBy(PmsMaterialRequisitionInfo::getMaterialRequisitionId));
            if (CollUtil.isNotEmpty(list)) {
                list.forEach(info -> map.put(info.getMaterialRequisitionId(), info.getQuantity()));
            }
        }
        return map;
    }

    @Override
    public Map<Long, BigDecimal> quantityMap(TupleDTO<LocalDate, Map<Long, Set<Integer>>> tupleDTO) {
        if (Objects.isNull(tupleDTO) || CollUtil.isEmpty(tupleDTO.getField2())) {
            return Maps.newHashMap();
        }
        //处理时间
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        if (Objects.nonNull(tupleDTO.getField1())) {
            startTime = LocalDateTime.of(tupleDTO.getField1(), LocalTime.MIN);
            endTime = LocalDateTime.of(tupleDTO.getField1(), LocalTime.MAX);
        }
        Map<Long, BigDecimal> map = Maps.newHashMap();
        //获取工位绑定的仓库
        Set<Integer> stationIds = Sets.newHashSet();
        Map<Long, Set<Integer>> field2 = tupleDTO.getField2();

        field2.forEach((key, value) -> {
            value.forEach(id -> {
                stationIds.add(id);
            });
        });
        Map<Integer, List<SysStationWarehouseVO>> warehouseMap = FeignOptional.of(() -> remoteStationWarehouseFeignApi.listByStationIds(stationIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //处理物料的工位数据
        Map<Long, List<Integer>> materialShelfMap = Maps.newHashMap();
        Map<Long, Set<Integer>> materialMap = tupleDTO.getField2();
        List<SysStationWarehouseVO> sysStationWarehouseVOList = Lists.newArrayList();
        materialMap.forEach((k, v) -> {
            List<Integer> shelfIds = Lists.newArrayList();
            v.forEach(id -> {
                List<SysStationWarehouseVO> orDefault = warehouseMap.getOrDefault(id, sysStationWarehouseVOList);
                orDefault.forEach(shelf -> shelfIds.add(shelf.getShelfId()));
            });
            if (CollUtil.isNotEmpty(shelfIds)) {
                materialShelfMap.put(k, shelfIds);
            }
        });
        if (CollUtil.isNotEmpty(materialShelfMap)) {
            //自定义sql获取领料数量
            List<MaterialRequisitionInfoVO> list = this.baseMapper.quantityList(startTime, endTime, materialShelfMap);
            if (CollUtil.isNotEmpty(list)) {
                List<MaterialRequisitionInfoVO> filter = list.stream().filter(infoVO -> infoVO.getReceiveQuantity() != null).collect(Collectors.toList());
                Map<Long, List<MaterialRequisitionInfoVO>> collect = filter.stream().collect(Collectors.groupingBy(MaterialRequisitionInfoVO::getMaterialId));
                for (Map.Entry<Long, List<MaterialRequisitionInfoVO>> entry : collect.entrySet()) {
                    BigDecimal quantity = BigDecimal.ZERO;
                    for (MaterialRequisitionInfoVO infoVO : entry.getValue()) {
                        quantity = quantity.add(infoVO.getReceiveQuantity());
                    }
                    map.put(entry.getKey(), quantity);
                }
            }
        }
        return map;
    }
}




