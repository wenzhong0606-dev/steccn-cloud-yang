package com.steccn.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsWarehouseMaterialAllocation;
import com.steccn.wms.dto.WmsWarehouseMaterialAllocationDTO;
import com.steccn.wms.mapper.WmsWarehouseMaterialAllocationMapper;
import com.steccn.wms.service.WmsWarehouseMaterialAllocationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author W10
* @description 针对表【wms_warehouse_material_allocation(仓储物料配置)】的数据库操作Service实现
* @createDate 2022-11-30 09:45:17
*/
@Service
public class WmsWarehouseMaterialAllocationServiceImpl extends ServiceImpl<WmsWarehouseMaterialAllocationMapper, WmsWarehouseMaterialAllocation>
    implements WmsWarehouseMaterialAllocationService{

    @Override
    public Page<WmsWarehouseMaterialAllocation> getMaterialConfigureList(WmsWarehouseMaterialAllocationDTO dto) {
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        LambdaQueryWrapper<WmsWarehouseMaterialAllocation> wmsWarehouseMaterialAllocationLambdaQueryWrapper = new LambdaQueryWrapper<WmsWarehouseMaterialAllocation>();
        wmsWarehouseMaterialAllocationLambdaQueryWrapper.eq(WmsWarehouseMaterialAllocation::getDelFlag,0);
        //搜索条件可以是专题中文名或英文名
        if (StringUtils.isNotEmpty(dto.getKeyWord())) {
            wmsWarehouseMaterialAllocationLambdaQueryWrapper.and(wrapper -> wrapper.like(WmsWarehouseMaterialAllocation::getMaterialCode, dto.getKeyWord())
                    .or().like(WmsWarehouseMaterialAllocation::getMaterialName, dto.getKeyWord()));
        }
        Page resPage = page(page, wmsWarehouseMaterialAllocationLambdaQueryWrapper);
        return resPage;
    }

    @Override
    public R<String> addOrUpdateMaterialConfigure(WmsWarehouseMaterialAllocation dto) {
        Integer result;
        if(dto.getId() == null ){
            dto.setCreateBy( SecurityUtils.getUser().getUserId());
            dto.setCreateName( SecurityUtils.getUser().getName());
            result = baseMapper.insert(dto);
        }else{
            WmsWarehouseMaterialAllocation wmsWarehouseMaterialAllocationTem = baseMapper.selectById(dto.getId());
            if(wmsWarehouseMaterialAllocationTem == null) return R.failure("未查询到此仓储物料配置信息");
            dto.setUpdateBy( SecurityUtils.getUser().getUserId());
            dto.setUpdateName( SecurityUtils.getUser().getName());
            result = baseMapper.updateById(dto);
        }
        return result >0 ?R.success("操作成功"):R.failure("操作失败");
    }

    @Override
    public R<String> delMaterialConfigure(WmsWarehouseMaterialAllocation dto) {
        WmsWarehouseMaterialAllocation wmsWarehouseMaterialAllocation = baseMapper.selectById(dto.getId());
        wmsWarehouseMaterialAllocation.setDelFlag(1);
        wmsWarehouseMaterialAllocation.setUpdateBy( SecurityUtils.getUser().getUserId());
        wmsWarehouseMaterialAllocation.setUpdateName( SecurityUtils.getUser().getName());
        int reulst = baseMapper.updateById(wmsWarehouseMaterialAllocation);
        return reulst>0?R.success("删除成功"):R.failure("删除失败");
    }
}




