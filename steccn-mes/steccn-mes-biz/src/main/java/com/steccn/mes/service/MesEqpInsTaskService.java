package com.steccn.mes.service;

import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.mes.domain.MesEqpInsTask;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;

/**
* @author Admin
* @description 针对表【mes_eqp_ins_task(点检任务表)】的数据库操作Service
* @createDate 2023-09-08 14:56:45
*/
public interface MesEqpInsTaskService extends IService<MesEqpInsTask> {
    /**
     * 获取任务完成数据
     * @param startTime
     * @param endTime
     * @return
     */
    QtLineChartBarDTO taskComplete(LocalDateTime startTime,LocalDateTime endTime,Integer type);

    /**
     * 点检项完成
     * @param startTime
     * @param endTime
     * @return
     */
    QtLineChartBarDTO itemComplete(LocalDateTime startTime,LocalDateTime endTime,Integer type);
}
