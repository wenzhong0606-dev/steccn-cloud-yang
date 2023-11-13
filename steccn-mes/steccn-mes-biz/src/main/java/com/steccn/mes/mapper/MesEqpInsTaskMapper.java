package com.steccn.mes.mapper;

import com.steccn.mes.domain.MesEqpInsTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.mes.dto.MesInsTaskCountDTO;
import com.steccn.mes.dto.MesInsTaskParamDTO;
import com.steccn.mes.dto.MesInsTaskTmpDTO;
import com.steccn.mes.dto.MesInsTaskTmpParamDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
}




