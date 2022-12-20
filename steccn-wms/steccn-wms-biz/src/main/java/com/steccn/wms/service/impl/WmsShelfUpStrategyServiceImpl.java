package com.steccn.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.common.vo.R;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.wms.domain.WmsShelfUpStrategy;
import com.steccn.wms.dto.WmsShelfUpStrategyDTO;
import com.steccn.wms.mapper.WmsShelfUpStrategyMapper;
import com.steccn.wms.service.WmsShelfUpStrategyService;
import com.steccn.wms.vo.WmsMaterialShelfUpVO;
import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author W10
 * @description 针对表【wms_shelf_up_strategy(上架策略)】的数据库操作Service实现
 * @createDate 2022-11-30 09:45:17
 */
@Service
@RequiredArgsConstructor
public class WmsShelfUpStrategyServiceImpl extends ServiceImpl<WmsShelfUpStrategyMapper, WmsShelfUpStrategy>
        implements WmsShelfUpStrategyService {

    private final RemoteSpareFeignApi remoteSpareFeignApi;

    private final RemoteOfficeFeign remoteOfficeFeign;

    /***
     * 获取上架策略列表
     * @param dto
     * @return
     */
    @Override
    public Page<WmsShelfUpStrategy> getShelfUpStrategyList(WmsShelfUpStrategyDTO dto) {
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        List<WmsShelfUpStrategy> wmsShelfUpStrategyVOList = baseMapper.getShelfUpStrategyList(page, dto);
        for (WmsShelfUpStrategy up : wmsShelfUpStrategyVOList) {
            List<Long> materialIds = new ArrayList<>();
            materialIds.add(up.getMaterialId());
            R<Map<Long, EqpSpareVO>> materialInfo = remoteSpareFeignApi.listById(materialIds);
            if (materialInfo.getData().size() > 0)
                up.setMaterialUnitName(materialInfo.getData().get(up.getMaterialId()).getUnit());
        }
        return page.setRecords(wmsShelfUpStrategyVOList);
    }

    /**
     * 新增或者编辑上架策略
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean addOrUpdateShelfUpStrategy(WmsShelfUpStrategy dto) {
        Integer result;
        if (Objects.isNull(dto.getId())) {
            result = baseMapper.insert(dto);
        } else {
            WmsShelfUpStrategy wmsShelfUpStrategy = baseMapper.selectById(dto.getId());
            if (wmsShelfUpStrategy == null) return false;
            result = baseMapper.updateById(dto);
        }
        return result > 0 ? true : false;
    }

    /**
     * 删除上架策略
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean delShelfUpStrategy(WmsShelfUpStrategy dto) {
        return removeById(dto);
    }

    @Override
    public Map<Long, WmsMaterialShelfUpVO> materialShelfUpInfo(Set<Long> materialIds) {
        Map<Long, WmsMaterialShelfUpVO> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(materialIds)) {
            List<WmsShelfUpStrategy> list = list(new LambdaQueryWrapper<WmsShelfUpStrategy>().in(WmsShelfUpStrategy::getMaterialId, materialIds));
            Map<Long, List<WmsShelfUpStrategy>> collect = list.stream().collect(Collectors.groupingBy(WmsShelfUpStrategy::getMaterialId));
            Set<Integer> officeIds = Sets.newHashSet();
            collect.forEach((materialId, shelfUp) -> {
                shelfUp.forEach(info -> {
                    List<BaseSelectVO> storageLocation = JSONArray.parseArray(info.getStorageLocation().toString(), BaseSelectVO.class);
                    storageLocation.forEach(location -> {
                        officeIds.add((Integer) location.getId());
                    });
                });
            });
            Map<Integer, SysOfficeVO> officeMap = FeignOptional.of(remoteOfficeFeign.listById(officeIds)).get();
            collect.forEach((materialId, shelfUp) -> {
                WmsMaterialShelfUpVO wmsMaterialShelfUpVO = new WmsMaterialShelfUpVO();
                wmsMaterialShelfUpVO.setMaterialId(materialId);
                shelfUp.forEach(info -> {
                    List<BaseSelectVO> storageLocation = JSONArray.parseArray(info.getStorageLocation().toString(), BaseSelectVO.class);
                    storageLocation.forEach(location -> {
                        SysOfficeVO sysOfficeVO = officeMap.get((Integer) location.getId());
                        if (Objects.nonNull(sysOfficeVO)) {
                            if (SysOfficeConstant.WAREHOUSE_TYPE.equals(sysOfficeVO.getOfficeType())
                                    && Objects.isNull(wmsMaterialShelfUpVO.getWarehouseId())) {
                                wmsMaterialShelfUpVO.setWarehouseId((Integer) location.getId());
                                wmsMaterialShelfUpVO.setWarehouseCode(sysOfficeVO.getOfficeCode());
                                wmsMaterialShelfUpVO.setWarehouseName(sysOfficeVO.getOfficeName());
                            }
                            if (SysOfficeConstant.SHELF_TYPE.equals(sysOfficeVO.getOfficeType())
                                    && Objects.isNull(wmsMaterialShelfUpVO.getShelfId())) {
                                wmsMaterialShelfUpVO.setShelfId((Integer) location.getId());
                                wmsMaterialShelfUpVO.setShelfName(sysOfficeVO.getOfficeName());
                                wmsMaterialShelfUpVO.setShelfCode(sysOfficeVO.getOfficeCode());
                            }
                        }
                    });
                });
                map.put(materialId, wmsMaterialShelfUpVO);
            });
        }
        return map;
    }
}




