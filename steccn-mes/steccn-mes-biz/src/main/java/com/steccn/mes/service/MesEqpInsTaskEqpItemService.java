package com.steccn.mes.service;

import com.steccn.common.dto.TupleDTO;
import com.steccn.mes.domain.MesEqpInsTaskEqpItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
* @author Admin
* @description 针对表【mes_eqp_ins_task_eqp_item(点检路线设备点检项关联表)】的数据库操作Service
* @createDate 2024-12-26 14:28:51
*/
public interface MesEqpInsTaskEqpItemService extends IService<MesEqpInsTaskEqpItem> {
    /**
     * 泡界线位置
     * @param day
     * @return
     */
    List<TupleDTO<LocalDateTime,String>> paoPosition(LocalDate day);
}
