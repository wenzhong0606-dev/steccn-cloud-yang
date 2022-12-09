package com.steccn.wms.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.wms.domain.WmsDeliveryReceipt;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.wms.dto.WmsDeliveryReceiptPageDTO;
import com.steccn.wms.vo.WmsDeliveryReceiptVO;
import org.apache.ibatis.annotations.Mapper;

/**
* @author W10
* @description 针对表【wms_delivery_receipt(出库单)】的数据库操作Mapper
* @createDate 2022-12-09 11:18:09
* @Entity com.steccn.wms.domain.WmsDeliveryReceipt
*/
@Mapper
public interface WmsDeliveryReceiptMapper extends BaseMapper<WmsDeliveryReceipt> {

    /**
     * 分页条件筛选
     * @param page
     * @param dto
     * @return
     */
    Page<WmsDeliveryReceiptVO> pageList(Page page, WmsDeliveryReceiptPageDTO dto);
}




