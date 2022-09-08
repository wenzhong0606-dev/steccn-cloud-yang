package com.steccn.qms.service;

import com.steccn.qms.domain.QtMaterialLinkCheckPlan;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.QtCheckPlanDTO;
import com.steccn.qms.dto.QtMaterialLinkCheckPlanDTO;

/**
* @author AndrewLuo
* @description 针对表【qt_material_link_check_plan(物料关联检验计划表)】的数据库操作Service
* @createDate 2022-09-02 08:54:09
*/
public interface QtMaterialLinkCheckPlanService extends IService<QtMaterialLinkCheckPlan> {
    /**
     * 绑定物料
     * @param dto
     * @return
     */
    Boolean add(QtMaterialLinkCheckPlanDTO dto);

}
