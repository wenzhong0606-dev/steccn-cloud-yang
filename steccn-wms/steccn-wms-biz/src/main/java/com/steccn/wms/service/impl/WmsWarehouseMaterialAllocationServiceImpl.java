package com.steccn.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.wms.domain.WmsWarehouseMaterialAllocation;
import com.steccn.wms.dto.WmsWarehouseMaterialAllocationDTO;
import com.steccn.wms.mapper.WmsWarehouseMaterialAllocationMapper;
import com.steccn.wms.service.WmsWarehouseMaterialAllocationService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
* @author W10
* @description 针对表【wms_warehouse_material_allocation(仓储物料配置)】的数据库操作Service实现
* @createDate 2022-11-30 09:45:17
*/
@Service
@RequiredArgsConstructor
public class WmsWarehouseMaterialAllocationServiceImpl extends ServiceImpl<WmsWarehouseMaterialAllocationMapper, WmsWarehouseMaterialAllocation>
    implements WmsWarehouseMaterialAllocationService{

    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final RemoteDictFeignApi remoteDictFeignApi;
    /****
     * 获取仓储物料配置列表分页
     * @param dto
     * @return
     */
    @Override
    public Page<WmsWarehouseMaterialAllocation> getMaterialConfigureList(WmsWarehouseMaterialAllocationDTO dto) {
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        LambdaQueryWrapper<WmsWarehouseMaterialAllocation> wmsWarehouseMaterialAllocationLambdaQueryWrapper = new LambdaQueryWrapper<WmsWarehouseMaterialAllocation>();
        wmsWarehouseMaterialAllocationLambdaQueryWrapper.eq(WmsWarehouseMaterialAllocation::getDelFlag,0);
        if (StringUtils.isNotEmpty(dto.getKeyWord())) {
            wmsWarehouseMaterialAllocationLambdaQueryWrapper.and(wrapper -> wrapper.like(WmsWarehouseMaterialAllocation::getMaterialCode, dto.getKeyWord())
                    .or().like(WmsWarehouseMaterialAllocation::getMaterialName, dto.getKeyWord()));
        }
        wmsWarehouseMaterialAllocationLambdaQueryWrapper.orderByDesc(WmsWarehouseMaterialAllocation::getCreateTime);
        Page resPage = page(page, wmsWarehouseMaterialAllocationLambdaQueryWrapper);
        List<WmsWarehouseMaterialAllocation> list = new ArrayList<>();
        resPage.getRecords().forEach(vo->{
            WmsWarehouseMaterialAllocation wmsWarehouseMaterialAllocation = new WmsWarehouseMaterialAllocation();
            BeanUtils.copyProperties(vo,wmsWarehouseMaterialAllocation);
            List<Long> materialIds = new ArrayList<>();
            materialIds.add(wmsWarehouseMaterialAllocation.getMaterialId());
            R<Map<Long, EqpSpareVO>> materialInfo = remoteSpareFeignApi.listById(materialIds);
            if(materialInfo.getData().size()>0) wmsWarehouseMaterialAllocation.setMaterialUnitName(materialInfo.getData().get(wmsWarehouseMaterialAllocation.getMaterialId()).getUnit());
            List<Long> deliveryPeriodUnits = new ArrayList<>();
            deliveryPeriodUnits.add(wmsWarehouseMaterialAllocation.getDeliveryPeriodUnit());
            R<Map<Long, SysDictVO>> sysDicts = remoteDictFeignApi.listById(deliveryPeriodUnits);
            if(sysDicts.getData().size()>0) wmsWarehouseMaterialAllocation.setDeliveryPeriodUnitName(sysDicts.getData().get(wmsWarehouseMaterialAllocation.getDeliveryPeriodUnit()).getName());
            list.add(wmsWarehouseMaterialAllocation);
        });
        resPage.setRecords(list);
        return resPage;
    }

    /***
     * 新增或者编辑获取仓储物料配置
     * @param dto
     * @return
     */
    @Override
    public R<Boolean> addOrUpdateMaterialConfigure(WmsWarehouseMaterialAllocation dto) {
        Integer result;
        LambdaQueryWrapper<WmsWarehouseMaterialAllocation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WmsWarehouseMaterialAllocation::getMaterialName,dto.getMaterialName());
        List<WmsWarehouseMaterialAllocation> allocationList = baseMapper.selectList(queryWrapper);
        if(allocationList.size() > 0){
            if (allocationList.size() > 1) return R.failure("物料名称不可重复");
            if(Objects.isNull(dto.getId()) && dto.getMaterialName().equals(allocationList.get(0).getMaterialName()))  {
                return R.failure("物料名称不可重复");
            }
        }
        if(Objects.isNull(dto.getId()) ){
            result = baseMapper.insert(dto);
        }else{
            WmsWarehouseMaterialAllocation wmsWarehouseMaterialAllocationTem = baseMapper.selectById(dto.getId());
            if(wmsWarehouseMaterialAllocationTem == null) return R.success(false);
            result = baseMapper.updateById(dto);
        }
        return result >0 ?R.success(true):R.success(false);
    }

    /***
     * 删除仓储物料配置
     * @param dto
     * @return
     */
    @Override
    public Boolean delMaterialConfigure(WmsWarehouseMaterialAllocation dto) {
        return removeById(dto);
    }
}




