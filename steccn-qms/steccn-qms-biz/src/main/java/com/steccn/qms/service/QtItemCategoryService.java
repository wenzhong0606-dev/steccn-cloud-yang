package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.domain.QtItemCategory;
import com.steccn.qms.dto.QtCheckItemStatusDTO;
import com.steccn.qms.dto.QtItemCategorySaveDTO;
import com.steccn.qms.vo.QtItemCategoryVO;

import java.util.List;

/**
 * @author yequan
 * @description 针对表【qt_item_category(类型表)】的数据库操作Service
 * @createDate 2022-09-02 08:54:09
 */
public interface QtItemCategoryService extends IService<QtItemCategory> {

    /**
     * 根据类型获取检验项和缺陷项的种类集合
     *
     * @param type 类别 0检验项 1缺陷项
     * @return 种类集合
     */
    List<QtItemCategoryVO> getCategoryList(Integer type);

    /**
     * 保存检验项和缺陷项的类别
     *
     * @param dto com.steccn.qms.dto
     * @return 是否成功
     */
    Boolean saveCategory(QtItemCategorySaveDTO dto);

    /**
     * 根据id获取信息
     *
     * @param id
     * @return
     */
    QtItemCategoryVO info(Integer id);

    /**
     * 缺陷项类别删除
     *
     * @param id 主键
     * @return
     */
    Boolean delete(Integer id);

    /**
     * 该类型下的类别id是否存在
     *
     * @param id   主键id
     * @param type 类型 见com.steccn.qms.constant.QtManagerConstant.QtItemCategoryEnum
     * @return 是否
     */
    Boolean existType(Integer id, Integer type);
}
