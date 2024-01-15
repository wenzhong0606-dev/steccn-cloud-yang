package com.steccn.mes.mapper;

import com.steccn.mes.domain.MesEqpInsTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.mes.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
* @author Admin
* @description 针对表【mes_eqp_ins_task(点检任务表)】的数据库操作Mapper
* @createDate 2023-09-08 14:56:45
* @Entity com.steccn.mes.domain.MesEqpInsTask
*/
@Mapper
public interface MesEqpInsTaskMapper extends BaseMapper<MesEqpInsTask> {
    /**
     * 获取任务统计数据
     * @param dto
     * @return
     */
    List<MesInsTaskCountDTO> findTaskCount(@Param("dto") MesInsTaskParamDTO dto);

    /**
     * 获取任务统计数据
     * @param dto
     * @return
     */
    List<MesInsTaskCountDTO> findInsCount(@Param("dto") MesInsTaskParamDTO dto);

    /**
     * 温度报表
     * @param dto
     * @return
     */
    List<MesInsTaskTmpDTO> findTmpValue(@Param("dto") MesInsTaskTmpParamDTO dto);

    /**
     * 根据部门获取各个状态的任务
     * @param departments
     * @return
     */
    List<MesEqpCountDTO> findTaskCount2(@Param("departments") Set<String> departments, @Param("startTime")LocalDateTime startTime,@Param("endTime")LocalDateTime endTime);
}




