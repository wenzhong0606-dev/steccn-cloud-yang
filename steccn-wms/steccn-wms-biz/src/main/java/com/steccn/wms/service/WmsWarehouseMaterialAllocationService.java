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


    /***
     * 获取仓储物料配置列表分页
     * @param dto
     * @return
     */
    Page<WmsWarehouseMaterialAllocation> getMaterialConfigureList(WmsWarehouseMaterialAllocationDTO dto);

    /***
     * 新增或者编辑获取仓储物料配置
     * @param dto
     * @return
     */
    R<Boolean> addOrUpdateMaterialConfigure(WmsWarehouseMaterialAllocation dto);

    /***
     * 删除仓储物料配置
     * @param dto
     * @return
     */
    Boolean delMaterialConfigure( WmsWarehouseMaterialAllocation dto);

}
