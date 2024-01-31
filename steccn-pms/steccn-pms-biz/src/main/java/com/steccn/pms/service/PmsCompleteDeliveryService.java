package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.Tuple4DTO;
import com.steccn.common.dto.Tuple5DTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.pms.domain.PmsCompleteDelivery;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.domain.PmsCompleteDeliveryInfo;
import com.steccn.pms.domain.PmsCompleteStorageInfo;
import com.steccn.pms.dto.PmsCompleteDeliveryDTO;
import com.steccn.pms.dto.PmsCompleteDeliveryInfoDTO;
import com.steccn.pms.dto.PmsDayReportDTO;
import com.steccn.pms.dto.ProcessReportWorkMaterialListDTO;
import com.steccn.pms.vo.PmsCompleteDeliveryVO;
import com.steccn.pms.vo.ProduceOrderProcessNumVO;
import com.steccn.pms.vo.SubstrateIngredientVO;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.dto.WmsDeliveryReceiptInfoDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_complete_delivery(生产订单-出库)】的数据库操作Service
 * @createDate 2022-09-28 15:18:51
 */
public interface PmsCompleteDeliveryService extends IService<PmsCompleteDelivery> {

    /**
     * 查询投料信息
     *
     * @param dto
     * @return
     */
    Page<PmsCompleteDeliveryVO> pageList(PmsCompleteDeliveryDTO dto);

    /**
     * 获取订单投料分页列表
     *
     * @param dto
     * @return
     */
    Page<PmsCompleteDeliveryVO> completeDeliveryPageList(PmsCompleteDeliveryDTO dto);


    /**
     * 工艺信息-投料信息
     *
     * @param dto
     * @return
     */
    Page<PmsCompleteDeliveryVO> feedPageList(PmsCompleteDeliveryDTO dto);

    /**
     * 工序物料消耗
     * @param produceOrderIdSet 生产订单id集合
     * @return map key-订单id value【key-工序id value【key-物料id，value-数据对象】】
     */
    Map<Long, Map<Long, Map<Long, ProduceOrderProcessNumVO>>> processNumConsumeMap(Set<Long> produceOrderIdSet);

    /**
     * 基片日报表
     * @param dto
     * @return
     */
    List<SubstrateIngredientVO> subIngredientReport(PmsDayReportDTO dto);

    /**
     * 获取物料消息信息
     * @param id 报工信息
     * @return
     */
    List<ProcessReportWorkMaterialListDTO> getMaterialConsume(Long id);

    /**
     * 生成生产出库单根据入库单单信息
     * @param pmsCompleteDelivery
     * @param wmsDeliveryReceiptDTO
     * @return
     */
    Boolean generateOrderByCompleteStorage(PmsCompleteDelivery pmsCompleteDelivery, WmsDeliveryReceiptDTO wmsDeliveryReceiptDTO);


    /**
     * 批量获取物料消耗信息
     * @param reportIds
     * @return
     */
    Map<Long,List<PmsCompleteDeliveryInfoDTO>> reportWorkMaterial(Set<Long> reportIds);


    /**
     * 获取报工物料消耗（异丙醇/镀膜液/油墨）
     * @param  dto 车间/部门id，开始日期，结束日期，粒度类型,部门：0/车间类型：1
     * @return key为-1表示合计，key为产线车间主键表示对应的产线车间的值，list[0]:异丙醇，list[1]:镀膜液，list[2]:油墨
     */
    List<TupleDTO<String,Map<Integer,BigDecimal>>> materialConsumption(Tuple5DTO<Integer, LocalDate, LocalDate, Integer,Integer> dto);
}
