package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.qms.domain.QtMaterial;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.QtCheckItemPageDTO;
import com.steccn.qms.dto.QtMaterialLinkCheckPlanDTO;
import com.steccn.qms.dto.QtMaterialPageDTO;
import com.steccn.qms.vo.QtCheckItemListVO;
import com.steccn.qms.vo.QtMaterialPageVO;

/**
* @author AndrewLuo
* @description 针对表【qt_material(物料表)】的数据库操作Service
* @createDate 2022-09-02 08:54:09
*/
public interface QtMaterialService extends IService<QtMaterial> {
    /**
     * 编辑绑定的方案和类型
     * @param dto
     * @return
     */
    Boolean edit(QtMaterialLinkCheckPlanDTO dto);

    /**
     * 检验项分页列表
     * @param dto 筛选条件，排序条件
     * @return 列表
     */
    Page<QtMaterialPageVO> pageList(QtMaterialPageDTO dto);

    /**
     * 回显
     * @param id
     * @return 列表
     */
    QtMaterial info(Long id);
}
