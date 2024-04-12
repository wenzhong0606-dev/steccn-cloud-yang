package com.steccn.wms.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.wms.domain.WmsDeliveryReceipt;
import com.steccn.wms.domain.WmsStorageReceipt;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.wms.domain.WmsStorageReceiptInfo;
import com.steccn.wms.dto.BatchChargeAgainstDTO;
import com.steccn.wms.dto.WmsStorageReceiptDTO;
import com.steccn.wms.dto.WmsStorageReceiptPageDTO;
import com.steccn.wms.vo.WmsDeliveryReceiptVO;
import com.steccn.wms.vo.WmsStorageReceiptVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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

    /**
     * 根据箱号 查询供应商
     * @param caseNoSet 箱号
     * @return
     */
    List<WmsStorageReceipt> caseNoLinkSupplier(@Param("caseNoSet") Set<String> caseNoSet);

    /**
     * 入库单分类分析
     * @param warehouseIds 仓库id集合
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    List<WmsStorageReceiptInfo> warehousingClassification(@Param("warehouseIds") Set<Long> warehouseIds, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);


    /**
     * 入库单分类分析
     * @param shelfIds 货架id集合
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    List<WmsStorageReceiptInfo> warehousingClassificationByShelfIds(@Param("shelfIds") Set<Integer> shelfIds, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);


    /**
     * 根据明细的关联单号获取单据信息
     * @param dtoList
     * @return
     */
    List<WmsDeliveryReceipt> listByAssociatedNo(@Param("dtoList") List<BatchChargeAgainstDTO> dtoList);

    /**
     * 区间数据
     * @param startTime
     * @param endTime
     * @return
     */
    List<WmsStorageReceiptInfo> intervalData(@Param("startTime") LocalDateTime startTime,@Param("endTime") LocalDateTime endTime,@Param("materialIds") Set<Long> materialIds);
}




