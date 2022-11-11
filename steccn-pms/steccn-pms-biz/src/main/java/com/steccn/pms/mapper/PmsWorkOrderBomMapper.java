package com.steccn.pms.mapper;

import com.steccn.common.dto.TupleDTO;
import com.steccn.pms.domain.PmsWorkOrderBom;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.domain.PmsWorkOrderCraftStation;
import com.steccn.pms.dto.BomStationDTO;
import com.steccn.pms.dto.PmsWorkOrderCraftStationDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
* @author yequan
* @description 针对表【pms_work_order_bom(生产工单-物料清单信息)】的数据库操作Mapper
* @createDate 2022-09-28 15:18:51
* @Entity com.steccn.pms.domain.PmsWorkOrderBom
*/
@Mapper
public interface PmsWorkOrderBomMapper extends BaseMapper<PmsWorkOrderBom> {

    /**
     * 根据bomId 获取绑定的工位信息
     * @param bomIds
     * @return
     */
    List<BomStationDTO> listStation(@Param("bomIds") Set<Long> bomIds);

    /**
     * 查看物料订单的工位，不区分物料
     * @param conditionList 条件
     * @return
     */
    List<TupleDTO<Long, String>> materialOrderStation(@Param("conditionList") List<TupleDTO<Long, Long>> conditionList);
}




