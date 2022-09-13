package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.qms.domain.QtCheckPlan;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.*;
import com.steccn.qms.vo.QtCheckPlanDetailsVO;
import com.steccn.qms.vo.QtCheckPlanVO;
import io.swagger.models.auth.In;

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

    /**
     * 检验项分页列表
     * @param dto 筛选条件，排序条件
     * @return 列表
     */
    Page<QtCheckPlanVO> pageList(QtCheckPlanPageDTO dto);

    /**
     * 编辑时回显
     * @param id taskId
     * @return QtCheckPlanDetailsVo
     */
    QtCheckPlanDetailsVO info(Integer id, Long taskId);

    /**
     * 编辑
     * @param dto
     * @return QtCheckPlanDetailsVo
     */
    Boolean edit(QtCheckPlanDTO dto);

    /**
     * 停用启用
     * @param dto dto
     * @return 是否成功
     */
    Boolean status(QtCheckPlanStausDTO dto);

    /**
     * 删除检验方案
     * @param id 主键id
     * @return 是否
     */
    Boolean delete(Integer id);

}
