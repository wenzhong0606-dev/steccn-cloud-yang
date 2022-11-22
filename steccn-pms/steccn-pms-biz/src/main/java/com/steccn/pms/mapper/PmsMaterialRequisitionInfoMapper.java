package com.steccn.pms.mapper;

import com.steccn.common.dto.TupleDTO;
import com.steccn.platform.vo.SysStationWarehouseVO;
import com.steccn.pms.domain.PmsMaterialRequisitionInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.vo.MaterialRequisitionInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_material_requisition_info(生产领用单详情)】的数据库操作Mapper
 * @createDate 2022-11-02 10:10:08
 * @Entity com.steccn.pms.domain.PmsMaterialRequisitionInfo
 */
@Mapper
public interface PmsMaterialRequisitionInfoMapper extends BaseMapper<PmsMaterialRequisitionInfo> {


    /**
     * 根据创建时间筛选当前物料的已完成的东西
     *
     * @param startTime
     * @param endTime
     * @param map
     * @return
     */
    List<MaterialRequisitionInfoVO> quantityList(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 @Param("map") Map<Long, List<Integer>> map);
}




