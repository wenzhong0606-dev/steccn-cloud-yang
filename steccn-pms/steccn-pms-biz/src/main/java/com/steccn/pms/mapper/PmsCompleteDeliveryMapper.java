package com.steccn.pms.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsCompleteDelivery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.dto.PmsCompleteDeliveryDTO;
import com.steccn.pms.dto.PmsDayReportDTO;
import com.steccn.pms.vo.PmsCompleteDeliveryVO;
import com.steccn.pms.vo.ProduceOrderProcessNumVO;
import com.steccn.pms.vo.SubstrateIngredientConsumeVO;
import com.steccn.pms.vo.SubstrateIngredientVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.core.parameters.P;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_complete_delivery(生产订单-出库)】的数据库操作Mapper
 * @createDate 2022-09-28 15:18:51
 * @Entity com.steccn.pms.domain.PmsCompleteDelivery
 */
@Mapper
public interface PmsCompleteDeliveryMapper extends BaseMapper<PmsCompleteDelivery> {


    /**
     * 根据时间 物料 工序id查询投料信息
     *
     * @param page
     * @param dto
     * @return
     */
    Page<PmsCompleteDeliveryVO> pageList(Page page, @Param("dto") PmsCompleteDeliveryDTO dto);

    /**
     * 生产订单工序物料消耗和计划数量
     *
     * @param produceOrderIdSet 订单集合
     * @return list
     */
    List<ProduceOrderProcessNumVO> processNumConsumeMap(@Param("produceOrderIdSet") Set<Long> produceOrderIdSet);

    /**
     * 基片日报表-耗料信息
     *
     * @param productLineIdList 产线id集合
     * @param classesIdList     班组
     * @param date
     * @return
     */
    List<SubstrateIngredientConsumeVO> subIngredientReport(@Param("productLineIdList") List<Integer> productLineIdList, @Param("classesIdList") List<Long> classesIdList, @Param("date") LocalDate date);
}




