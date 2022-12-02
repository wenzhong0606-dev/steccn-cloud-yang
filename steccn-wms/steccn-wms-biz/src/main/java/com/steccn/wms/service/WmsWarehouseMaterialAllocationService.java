package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsWarehouseMaterialAllocation;
import com.steccn.wms.dto.WmsWarehouseMaterialAllocationDTO;

/**
* @author W10
* @description 针对表【wms_warehouse_material_allocation(仓储物料配置)】的数据库操作Service
* @createDate 2022-11-30 09:45:17
*/
public interface WmsWarehouseMaterialAllocationService extends IService<WmsWarehouseMaterialAllocation> {


    Page<WmsWarehouseMaterialAllocation> getMaterialConfigureList(WmsWarehouseMaterialAllocationDTO dto);

    R<Boolean> addOrUpdateMaterialConfigure( WmsWarehouseMaterialAllocation dto);

    R<Boolean> delMaterialConfigure( WmsWarehouseMaterialAllocation dto);

}
