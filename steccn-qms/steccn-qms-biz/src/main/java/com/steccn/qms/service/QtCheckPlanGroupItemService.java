package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.qms.domain.QtCheckPlanGroupItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.QtCheckItemPageDTO;
import com.steccn.qms.vo.QtCheckItemListVO;
import com.steccn.qms.vo.QtCheckTaskDefectVO;

import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【qt_check_plan_group_item(检验方案分组检验项目表)】的数据库操作Service
 * @createDate 2022-09-02 08:54:09
 */
public interface QtCheckPlanGroupItemService extends IService<QtCheckPlanGroupItem> {

    /**
     * 正在使用的检验项
     * @return 检验项id集合
     */
    Set<Integer> inUseCheckItem();

    /**
     * 正在使用的缺陷项
     * @return 缺陷项id集合
     */
    Set<Integer> inUseDefectiveItem();

    /**
     * 找到任务下的缺陷项
     * @param id
     * @return
     */
    List<QtCheckTaskDefectVO> findTaskDefect(Integer id);

}
