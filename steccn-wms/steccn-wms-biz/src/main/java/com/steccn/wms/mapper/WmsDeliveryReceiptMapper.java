package com.steccn.wms.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.wms.domain.WmsDeliveryReceipt;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.wms.domain.WmsDeliveryReceiptInfo;
import com.steccn.wms.dto.SalesLogisticsBillDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptPageDTO;
import com.steccn.wms.vo.SalesLogisticsBillVO;
import com.steccn.wms.vo.WmsDeliveryReceiptInfoVO;
import com.steccn.wms.vo.WmsDeliveryReceiptVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
    Page<WmsDeliveryReceiptVO> pageList(@Param("page") Page page,@Param("dto") WmsDeliveryReceiptPageDTO dto);

    /**
     * 报工关联箱号
     * @param rwIdSet 报工id主键
     * @return
     */
    List<WmsDeliveryReceipt> rwLinkCaseNo(@Param("rwIdSet") Set<Long> rwIdSet);

    /**
     * 根据仓库获取入库单
     * @param warehouseIds 仓库id集合
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    List<WmsDeliveryReceiptInfo> outboundDeliveryClassification(@Param("warehouseIds") Set<Long> warehouseIds, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    /**
     * 根据货架获取入库单
     * @param shelfIds 仓库id集合
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    List<WmsDeliveryReceiptInfo> outboundDeliveryClassificationByShelfIds(@Param("shelfIds")Set<Integer> shelfIds,@Param("startTime") LocalDateTime startTime,@Param("endTime")  LocalDateTime endTime);

    /**
     * 获取区间的数据
     * @param startTime
     * @param endTime
     * @return
     */
    List<WmsDeliveryReceiptInfo> intervalData(@Param("startTime") LocalDateTime startTime,@Param("endTime")  LocalDateTime endTime,@Param("materialIds") Set<Long> materialIds);

    List<WmsDeliveryReceiptInfoVO> intervalDataVO(@Param("startTime") LocalDateTime startTime,@Param("endTime")  LocalDateTime endTime);

    Page<SalesLogisticsBillVO> shippingReconciliation(Page page,@Param("dto") SalesLogisticsBillDTO dto);
}




