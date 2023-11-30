package com.steccn.sales.mapper;

import com.steccn.sales.domain.KsLogisticsOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.sales.dto.KsLogisticsDashBoardDTO;
import org.apache.ibatis.annotations.Mapper;

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
     * @param warehouseName 仓库名称
     * @param date 日期
     * @return
     */
    List<KsLogisticsDashBoardDTO> selectLogisticsDashBoard(String warehouseName, LocalDate date);

}




