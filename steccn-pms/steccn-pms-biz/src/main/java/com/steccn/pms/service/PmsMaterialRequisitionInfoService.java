package com.steccn.pms.service;

import com.steccn.pms.domain.PmsMaterialRequisitionInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.MaterialRequisitionDTO;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
* @author yequan
* @description 针对表【pms_material_requisition_info(生产领用单详情)】的数据库操作Service
* @createDate 2022-11-02 10:10:08
*/
public interface PmsMaterialRequisitionInfoService extends IService<PmsMaterialRequisitionInfo> {

    /**
     * 列表编辑
     * @param materialRequisitionDTO
     * @return
     */
    Boolean editList(MaterialRequisitionDTO materialRequisitionDTO);

    /**
     * 根据领料单id 获取总数量
     * @param ids
     * @return
     */
    Map<Long, BigDecimal> getTotalQuantity(Set<Long> ids);
}
