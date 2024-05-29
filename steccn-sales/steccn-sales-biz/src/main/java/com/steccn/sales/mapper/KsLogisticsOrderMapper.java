package com.steccn.sales.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.sales.domain.KsLogisticsOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.sales.dto.KsLogisticsDTO;
import com.steccn.sales.dto.KsLogisticsDashBoardDTO;
import com.steccn.sales.dto.KsShippingReconciliationDTO;
import com.steccn.sales.vo.KsShippingReconciliationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
* @author Admin
* @description 针对表【ks_logistics_order(物流单)】的数据库操作Mapper
* @createDate 2023-11-29 14:16:04
* @Entity com.steccn.sales.domain.KsLogisticsOrder
*/
@Mapper
public interface KsLogisticsOrderMapper extends BaseMapper<KsLogisticsOrder> {

    /**
     * 物流单看板
     * @param dto
     * @return
     */
    List<KsLogisticsDashBoardDTO> selectLogisticsDashBoard(@Param("dto") KsLogisticsDTO dto);

    /**
     * 销售发货
     * @param page
     * @param dto
     * @return
     */
    Page<KsShippingReconciliationVO> pageList(Page page,@Param("dto") KsShippingReconciliationDTO dto);
}




