package com.steccn.wms.mapper;

import com.steccn.wms.domain.WmsStorageReceiptInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
* @author W10
* @description 针对表【wms_storage_receipt_info】的数据库操作Mapper
* @createDate 2022-12-09 11:18:09
* @Entity com.steccn.wms.domain.WmsStorageReceiptInfo
*/
@Mapper
public interface WmsStorageReceiptInfoMapper extends BaseMapper<WmsStorageReceiptInfo> {


    /**
     * 根据箱号获取入库单明细并且不包括已冲销和已删除
     * @param caseNo
     * @return
     */
    WmsStorageReceiptInfo getByCaseNo(@Param("caseNo") String caseNo);


    /**
     * 获取类型为 采购入库的指定物料在时间范围内的数据
     * @param typeId
     * @param startTime
     * @param endTime
     * @param materialName
     * @return
     */
    List<WmsStorageReceiptInfo> purchaseQuantity(@Param("typeId") Long typeId, @Param("startTime") LocalDateTime startTime,@Param("endTime") LocalDateTime endTime,@Param("materialName") List<String> materialName);
}




