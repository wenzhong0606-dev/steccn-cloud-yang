package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.qms.domain.QtDefectProduct;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.QtDefectProductDTO;
import com.steccn.qms.dto.QtDefectProductPageDTO;
import com.steccn.qms.vo.QtDefectProductVO;

/**
* @author yequan
* @description 针对表【qms_defect_product(不合格品处理表)】的数据库操作Service
* @createDate 2023-03-15 10:16:54
*/
public interface QtDefectProductService extends IService<QtDefectProduct> {

    /**
     * 新增不合格品处理记录
     * @param dto
     * @return
     */
    Boolean add(QtDefectProductDTO dto);

    /**
     * 不合格品处理记录列表
     * @param dto
     * @return
     */
    Page<QtDefectProductVO> listPage(QtDefectProductPageDTO dto);


    /**
     * 编辑
     * @param dto
     * @return
     */
    Boolean edit(QtDefectProductDTO dto);

    /**
     * 删除
     * @param id
     * @return
     */
    Boolean delete(Long id);


    /**
     * 处理
     * @param dto
     * @return
     */
    Boolean dispose(QtDefectProductDTO dto);


    /**
     * 获取详情
     * @param id
     * @return
     */
    QtDefectProductVO get(Long id);
}
