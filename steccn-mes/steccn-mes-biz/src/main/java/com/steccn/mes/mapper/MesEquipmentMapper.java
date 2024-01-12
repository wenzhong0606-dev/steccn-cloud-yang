package com.steccn.mes.mapper;

import com.steccn.mes.domain.MesEquipment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.mes.dto.MesEqpCountDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
* @author Admin
* @description 针对表【mes_equipment(设备表
)】的数据库操作Mapper
* @createDate 2023-09-01 16:29:57
* @Entity com.steccn.mes.domain.MesEquipment
*/
@Mapper
public interface MesEquipmentMapper extends BaseMapper<MesEquipment> {

    /**
     * 找到各个设备状态的数量
     * @param departments
     * @return
     */
    List<MesEqpCountDTO> findEqpCount(@Param("departments")Set<String> departments);
}




