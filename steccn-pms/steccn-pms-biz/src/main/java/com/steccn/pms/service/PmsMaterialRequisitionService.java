package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsMaterialRequisition;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.*;
import com.steccn.pms.vo.MaterialRequisitionPlanVO;
import com.steccn.pms.vo.MaterialRequisitionVO;

import java.util.List;

/**
* @author yequan
* @description 针对表【pms_material_requisition(生产领料单)】的数据库操作Service
* @createDate 2022-11-02 10:10:08
*/
public interface PmsMaterialRequisitionService extends IService<PmsMaterialRequisition> {

    /**
     * 领料单分页列表
     * @param materialRequisitionPageDTO
     * @return
     */
    Page<MaterialRequisitionVO> pageList(MaterialRequisitionPageDTO materialRequisitionPageDTO);

    /**
     * 新增领料单
     * @param materialRequisitionDTO
     * @return
     */
    Boolean add(MaterialRequisitionDTO materialRequisitionDTO);

    /**
     * 编辑领料单
     * @param materialRequisitionDTO
     * @return
     */
    Boolean edit(MaterialRequisitionDTO materialRequisitionDTO);

    /**
     * 删除领料单
     * @param id
     * @return
     */
    Boolean delete(Long id);

    /**
     * 获取领料单详情
     * @param id
     * @return
     */
    MaterialRequisitionVO get(Long id);

    /**
     * 领料单审批
     * @param dto
     * @return
     */
    Boolean approval(MaterialRequisitionApprovalDTO dto);

    /**
     * 领料计划
     * @param dto
     * @return
     */
    Page<MaterialRequisitionPlanVO> planPageList(MaterialRequisitionPlanDTO dto);

    /**
     * 领料计划创建领料单
     * @param list
     * @return
     */
    String create( List<MaterialRequisitionInfoPlanDTO> list);
}
