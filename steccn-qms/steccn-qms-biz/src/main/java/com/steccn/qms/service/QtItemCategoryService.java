package com.steccn.qms.service;

import com.steccn.common.vo.R;
import com.steccn.qms.domain.QtItemCategory;
import com.baomidou.mybatisplus.extension.service.IService;
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
     * @param type 类别 0检验项 1缺陷项
     * @return 种类集合
     */
    List<QtItemCategoryVO> getCategoryList(Integer type);

    /**
     * 保存检验项和缺陷项的类别
     * @param dto com.steccn.qms.dto
     * @return 是否成功
     */
    Boolean saveCategory(QtItemCategorySaveDTO dto);

    /**
     * 根据id获取信息
     * @param id
     * @return
     */
    QtItemCategoryVO info(Integer id);
}
