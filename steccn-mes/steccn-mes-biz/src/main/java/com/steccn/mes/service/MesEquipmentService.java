package com.steccn.mes.service;

import com.steccn.mes.domain.MesEquipment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.mes.vo.MesEqpVo;

import java.util.List;

/**
* @author Admin
* @description 针对表【mes_equipment(设备表
)】的数据库操作Service
* @createDate 2023-09-01 16:29:57
*/
public interface MesEquipmentService extends IService<MesEquipment> {
    List<MesEqpVo> getEqpInfo();
}
