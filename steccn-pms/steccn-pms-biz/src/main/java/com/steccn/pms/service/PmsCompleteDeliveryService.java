package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsCompleteDelivery;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsCompleteDeliveryDTO;
import com.steccn.pms.dto.PmsDayReportDTO;
import com.steccn.pms.vo.PmsCompleteDeliveryVO;
import com.steccn.pms.vo.ProduceOrderProcessNumVO;
import com.steccn.pms.vo.SubstrateIngredientVO;

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
}
