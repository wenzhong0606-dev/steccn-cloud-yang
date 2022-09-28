package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.qms.domain.QtCheckPlan;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.*;
import com.steccn.qms.vo.QtCheckPlanDetailsVO;
import com.steccn.qms.vo.QtCheckPlanListAllVO;
import com.steccn.qms.vo.QtCheckPlanVO;
import io.swagger.models.auth.In;

import java.util.List;
import java.util.Set;

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
     * @param id taskId copy
     * @return QtCheckPlanDetailsVo
     */
    QtCheckPlanDetailsVO info(Integer id, Long taskId,Integer copy);

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

    /**
     * 查询所有检验项的名字和id
     * @return
     */
    List<QtCheckPlanListAllVO> listAll();

    /**
     *  给检验计划做快照
     *  1.将当前检验计划全部复克并且将旧的检验计划标识为用户不可见
     *  2.将物料的检验计划绑定成当前新的检验计划
     *  3.考虑检验项和缺陷项的快照问题
     * @param planId 计划id
     * @return 新的快照计划信息
     */
    QtCheckPlan planSnapshot(Integer planId);

    /**
     * 检验项，缺陷项关联的计划id
     * @param itemId 检验项或者缺陷项id
     * @param type 0表示检验项 1表示缺陷项
     * @return 计划的集合，可能为[]
     */
    Set<Integer> linkedPlan(Integer itemId , Integer type);

    /**
     * 给检验方案升级版本
     * @param planIdList 检验方案id的集合
     */
    void upVersion(Set<Integer> planIdList);
}
