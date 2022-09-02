package com.steccn.qms.service;

import vo.QtItemCategoryVO;

import java.util.List;

/**
 * @desc 后台配置服务接口
 */
public interface QtBackendService {

    /**
     * 根据类型获取检验项和缺陷项的种类集合
     * @param type 类别 0检验项 1缺陷项
     * @return 种类集合
     */
    List<QtItemCategoryVO> getCategoryList(Integer type);
}
