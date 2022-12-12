package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.wms.domain.WmsStorageReceipt;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.dto.WmsStorageReceiptDTO;
import com.steccn.wms.dto.WmsStorageReceiptPageDTO;
import com.steccn.wms.vo.WmsDeliveryReceiptVO;
import com.steccn.wms.vo.WmsStorageReceiptVO;

/**
* @author W10
* @description 针对表【wms_storage_receipt】的数据库操作Service
* @createDate 2022-12-09 11:18:09
*/
public interface WmsStorageReceiptService extends IService<WmsStorageReceipt> {

    /**
     * 分页列表
     * @param dto
     * @return
     */
    Page<WmsStorageReceiptVO> pageList(WmsStorageReceiptPageDTO dto);

    /**
     * 新增
     * @param dto
     * @return
     */
    Boolean add(WmsStorageReceiptDTO dto);

    /**
     * 编辑
     * @param dto
     * @return
     */
    Boolean edit(WmsStorageReceiptDTO dto);

    /**
     * 确认
     * @param id
     * @return
     */
    Boolean affirm(Long id);

    /**
     * 删除
     * @param id
     * @return
     */
    Boolean delete(Long id);

    /**
     * 获取详情内容
     * @param id
     * @return
     */
    WmsStorageReceiptVO get(Long id);
}
