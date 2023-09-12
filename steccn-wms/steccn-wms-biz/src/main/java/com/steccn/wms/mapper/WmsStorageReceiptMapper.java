package com.steccn.wms.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.wms.domain.WmsStorageReceipt;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.wms.dto.WmsStorageReceiptDTO;
import com.steccn.wms.dto.WmsStorageReceiptPageDTO;
import com.steccn.wms.vo.WmsDeliveryReceiptVO;
import com.steccn.wms.vo.WmsStorageReceiptVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author W10
* @description 针对表【wms_storage_receipt】的数据库操作Mapper
* @createDate 2022-12-09 11:18:09
* @Entity com.steccn.wms.domain.WmsStorageReceipt
*/
@Mapper
public interface WmsStorageReceiptMapper extends BaseMapper<WmsStorageReceipt> {

    /**
     * 分页列表
     * @param page
     * @param dto
     * @return
     */
    Page<WmsStorageReceiptVO> pageList(@Param("page") Page page, @Param("dto") WmsStorageReceiptPageDTO dto);

    /**
     * 销售系统传参自动确认
     * @param dto
     * @return
     */
    Boolean salesAutoConfirm(@Param("dto") WmsStorageReceiptDTO dto);
}




