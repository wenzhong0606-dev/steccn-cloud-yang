package com.steccn.qms.service;

import com.steccn.qms.domain.QtCheckPlan;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.QtCheckItemSaveDTO;
import com.steccn.qms.dto.QtCheckPlanDTO;

/**
* @author yequan
* @description 针对表【qt_check_plan(检验方案表)】的数据库操作Service
* @createDate 2022-09-02 08:54:09
*/
public interface QtCheckPlanService extends IService<QtCheckPlan> {
    /**
     * 检验计划dto
     * @param dto
     * @return
     */
    Boolean add(QtCheckPlanDTO dto);
}
