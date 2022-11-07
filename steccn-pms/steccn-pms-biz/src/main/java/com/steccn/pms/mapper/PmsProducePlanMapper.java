package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmsProducePlan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.vo.PmsProducePlanVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
* @author yequan
* @description 针对表【pms_produce_plan(生产月计划)】的数据库操作Mapper
* @createDate 2022-11-02 10:10:08
* @Entity com.steccn.pms.domain.PmsProducePlan
*/
@Mapper
public interface PmsProducePlanMapper extends BaseMapper<PmsProducePlan> {

    List<PmsProducePlanVO> selectAllByPlanDate(@Param("monthDate") String monthDate);

    List<PmsProducePlan> selectAllByPlanDateAndCode(@Param("planDate") String planDate,@Param("materialCode") String materialCode);

    /**
     * 根据物料id以及时间批量修改状态
     * @param materialIdList
     * @param firstDay
     * @param lastDay
     * @param status
     * @return
     */
    Boolean updateBatchStatus(@Param("materialIdList") List<Long> materialIdList,@Param("firstDay") LocalDate firstDay,@Param("lastDay") LocalDate lastDay, @Param("status") Integer status);
}




