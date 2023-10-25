package com.steccn.mes.mapper;

import com.steccn.mes.domain.MesDefect;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.mes.dto.MesDefectCountDTO;
import com.steccn.mes.dto.MesInsTaskCountDTO;
import com.steccn.mes.dto.MesInsTaskParamDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Admin
* @description 针对表【mes_defect(缺陷管理表)】的数据库操作Mapper
* @createDate 2023-10-20 16:01:23
* @Entity com.steccn.mes.domain.MesDefect
*/
@Mapper
public interface MesDefectMapper extends BaseMapper<MesDefect> {
    /**
     * 统计缺陷数据
     * @param dto
     * @return
     */
    List<MesDefectCountDTO> findDefectCount(@Param("dto") MesInsTaskParamDTO dto);
}




