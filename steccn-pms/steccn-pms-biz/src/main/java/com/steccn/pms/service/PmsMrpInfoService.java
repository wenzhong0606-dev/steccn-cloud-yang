package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsMrpInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.domain.PmsProduceOrder;
import com.steccn.pms.domain.PmsProducePlan;
import com.steccn.pms.dto.PmsMrpInfoPageDTO;
import com.steccn.pms.dto.PmsProduceOrderDTO;
import com.steccn.pms.dto.PmsProducePlanDTO;
import com.steccn.pms.dto.PmsProducePlanOrderDTO;
import com.steccn.pms.vo.PmsMrpInfoVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_mrp_info(物料需求计划详情)】的数据库操作Service
 * @createDate 2022-11-02 10:10:08
 */
public interface PmsMrpInfoService extends IService<PmsMrpInfo> {

//    /**
//     * 增加物料需求计划详情
//     *
//     * @param dto
//     * @return
//     */
//    Boolean add(PmsProducePlanDTO dto);

    /**
     * 下达进行mrp运算
     *
     * @param dto
     * @return
     */
    Boolean mrpAdd(PmsProducePlanOrderDTO dto);

    /**
     * 保存物料需求计划数据
     * @param addProducePlanList
     * @return
     */
    Boolean saveMrp(List<PmsProducePlan> addProducePlanList);

    /**
     * 获取物料需求计划详情分页数据
     * @param dto
     * @return
     */
    Page<PmsMrpInfoVO> infoPage(PmsMrpInfoPageDTO dto);

    /**
     * 转生产订单生成生产订单需求信息
     * @param model
     */
    void turnProduceOrder(PmsProduceOrderDTO model);


    /**
     * 创建pmsMrpInfo信息
     * @param type
     * @param quantity
     * @param availableQuantity
     * @param materialId
     * @param mrpDate
     * @param planId
     * @param status
     * @param mrpElement
     * @return
     */
    PmsMrpInfo create(String type, BigDecimal quantity, BigDecimal availableQuantity, Long materialId, LocalDate mrpDate, Long planId, Integer status,String mrpElement);

}
