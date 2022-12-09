package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.wms.domain.WmsDeliveryReceipt;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptPageDTO;
import com.steccn.wms.vo.WmsDeliveryReceiptVO;

/**
* @author W10
* @description 针对表【wms_delivery_receipt(出库单)】的数据库操作Service
* @createDate 2022-12-09 11:18:09
*/
public interface WmsDeliveryReceiptService extends IService<WmsDeliveryReceipt> {

    /**
     * 分页列表
     * @param dto
     * @return
     */
    Page<WmsDeliveryReceiptVO> pageList(WmsDeliveryReceiptPageDTO dto);

    /**
     * 新增
     * @param dto
     * @return
     */
    Boolean add(WmsDeliveryReceiptDTO dto);

    /**
     * 编辑
     * @param dto
     * @return
     */
    Boolean edit(WmsDeliveryReceiptDTO dto);

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
     * 获取详情
     * @param id
     * @return
     */
    WmsDeliveryReceiptVO get(Long id);
}
