package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsDeliveryReceipt;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptPageDTO;
import com.steccn.wms.vo.WmsDeliveryReceiptVO;
import com.steccn.wms.vo.WmsScanInventoryVO;

import java.util.Map;

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

    /**
     * 生成出库单
     * @param dto
     * @return
     */
    Boolean generateDelivery(WmsDeliveryReceiptDTO dto);

    /*****
     * 扫描 根据箱号获取库存信息
     * @param caseNo
     * @return
     */
    R<WmsScanInventoryVO> getCaseNoByScanInventoryInfo(String caseNo);

    /****
     * 手机端 扫描出库
     * @param paramMap
     * @return
     */
    R<String> scanDeliveryReceipt( Map<String, Object> paramMap);
}
