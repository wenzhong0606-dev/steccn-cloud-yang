package com.steccn.dp.mapper;

import com.steccn.dp.domain.WeightCount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.dp.vo.DpValueDateVO;
import com.steccn.pms.dto.PmsPullForceDayCountDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
* @author Admin
* @description 针对表【weight_count(生产工序表)】的数据库操作Mapper
* @createDate 2024-08-27 15:44:50
* @Entity com.steccn.dp.domain.WeightCount
*/
@Mapper
public interface WeightCountMapper extends BaseMapper<WeightCount> {
    /**
     * 聚合一天的电子称读数(日)
     * @param start 开始时间
     * @param end 结束时间
     * @return
     */
    List<DpValueDateVO> selectWeightCountByDay(@Param("start") LocalDate start, @Param("end")LocalDate end);

    /**
     * 聚合一天的电子称读数(周)
     * @param start 开始时间
     * @param end 结束时间
     * @return
     */
    List<DpValueDateVO> selectWeightCountByWeek(@Param("start")LocalDate start, @Param("end")LocalDate end);

    /**
     * 聚合一天的电子称读数(月)
     * @param start 开始时间
     * @param end 结束时间
     * @return
     */
    List<DpValueDateVO> selectWeightCountByMonth(@Param("start")LocalDate start, @Param("end")LocalDate end);

    /**
     * 聚合一天的电子称读数(年)
     * @param start 开始时间
     * @param end 结束时间
     * @return
     */
    List<DpValueDateVO> selectWeightCountByYear(@Param("start")LocalDate start, @Param("end")LocalDate end);

}




