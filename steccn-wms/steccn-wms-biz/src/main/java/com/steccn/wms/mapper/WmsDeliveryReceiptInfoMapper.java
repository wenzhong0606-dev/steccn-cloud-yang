package com.steccn.wms.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.wms.domain.WmsDeliveryReceiptInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.wms.dto.WmsDeliveryReceiptPageDTO;
import com.steccn.wms.vo.WmsDeliveryReceiptInfoVO;
import com.steccn.wms.vo.WmsDeliveryReceiptVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author W10
* @description 针对表【wms_delivery_receipt_info(出库单明细)】的数据库操作Mapper
* @createDate 2022-12-09 11:18:09
* @Entity com.steccn.wms.domain.WmsDeliveryReceiptInfo
*/
@Mapper
public interface WmsDeliveryReceiptInfoMapper extends BaseMapper<WmsDeliveryReceiptInfo> {


    /**
     * 获取明细列表
     * @param id
     * @return
     */
    List<WmsDeliveryReceiptInfoVO> infoList(@Param("id") Long id);
}




