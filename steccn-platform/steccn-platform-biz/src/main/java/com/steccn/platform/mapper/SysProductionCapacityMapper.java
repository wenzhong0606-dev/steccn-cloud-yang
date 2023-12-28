package com.steccn.platform.mapper;

import com.steccn.platform.domain.SysProductionCapacity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.platform.vo.SysProductionCapacityDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【sys_production_capacity(标准产能表)】的数据库操作Mapper
 * @createDate 2023-12-19 16:50:56
 * @Entity com.steccn.platform.domain.SysProductionCapacity
 */
@Mapper
public interface SysProductionCapacityMapper extends BaseMapper<SysProductionCapacity> {

    /**
     * 查询工位指定日期区间的标准产能
     *
     * @param stationIdSet 工位主键集合
     * @param start        开始日期
     * @param end          结束日期
     * @return
     */
    List<SysProductionCapacityDetailVO> capacityStation(@Param("stationIdSet") Set<Integer> stationIdSet,
                                                        @Param("start") LocalDate start,
                                                        @Param("end") LocalDate end);
}




