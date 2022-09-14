package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.qms.domain.QtDefectiveItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.QtDefectiveItemPageDTO;
import com.steccn.qms.dto.QtDefectiveItemSaveDTO;
import com.steccn.qms.dto.QtItemStatusDTO;
import com.steccn.qms.vo.QtDefectiveItemTreeVo;
import com.steccn.qms.vo.QtDefectiveItemVO;

import java.util.List;

/**
 * @author yequan
 * @description 针对表【qt_defective_item(缺陷项表)】的数据库操作Service
 * @createDate 2022-09-02 08:54:09
 */
public interface QtDefectiveItemService extends IService<QtDefectiveItem> {

    /**
     * 缺陷项分页列表
     *
     * @param dto dto
     * @return 分页数据
     */
    Page<QtDefectiveItemVO> pageList(QtDefectiveItemPageDTO dto);

    /**
     * 新建缺陷项
     *
     * @param dto dto
     * @return 是否
     */
    Boolean add(QtDefectiveItemSaveDTO dto);

    /**
     * 编辑缺陷项
     *
     * @param dto dto
     * @return 是否成功
     */
    Boolean edit(QtDefectiveItemSaveDTO dto);

    /**
     * 停用启用
     *
     * @param dto dto
     * @return 是否成功
     */
    Boolean status(QtItemStatusDTO dto);

    /**
     * 删除缺陷项
     * @param id id
     * @return 是否成功
     */
    Boolean delete(Integer id);

    /**
     * 启用状态缺陷项列表
     * @return list
     */
    List<QtDefectiveItemTreeVo> selectItemList();
}
