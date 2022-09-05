package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.qms.domain.QtCheckItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.QtCheckItemAddDTO;
import com.steccn.qms.dto.QtCheckItemPageDTO;
import com.steccn.qms.vo.QtCheckItemPageVO;

import java.util.List;

/**
* @author yequan
* @description 针对表【qt_check_item(检验项表)】的数据库操作Service
* @createDate 2022-09-02 08:54:09
*/
public interface QtCheckItemService extends IService<QtCheckItem> {

    /**
     * 检验项分页列表
     * @param dto 筛选条件，排序条件
     * @return 列表
     */
    Page<QtCheckItemPageVO> pageList(QtCheckItemPageDTO dto);

    /**
     * 新建检验项
     * @param dto
     * @return
     */
    Boolean add(QtCheckItemAddDTO dto);
}
