package com.steccn.mes.service;

import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.mes.domain.MesDefect;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.mes.dto.MesDefectChartDTO;

import java.time.LocalDateTime;

/**
* @author Admin
* @description 针对表【mes_defect(缺陷管理表)】的数据库操作Service
* @createDate 2023-10-20 16:01:23
*/
public interface MesDefectService extends IService<MesDefect> {
    /**
     * 缺陷图表
     * @param dto
     * @return
     */
    QtLineChartBarDTO defectChart(MesDefectChartDTO dto);
}
