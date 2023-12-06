package com.steccn.dp.service;

import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.dp.domain.AdsOpcUaAll3;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.dp.dto.DpPmsOperateDTO;
import com.steccn.dp.vo.DpValueDateVO;
import com.steccn.dp.vo.DpWeightVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
* @author Admin
* @description 针对表【ads_opc_ua_all3(tagId包含Raw_YL.ZHM.fact_weigh的数据)】的数据库操作Service
* @createDate 2023-11-23 09:27:09
*/
public interface AdsOpcUaAll3Service extends IService<AdsOpcUaAll3> {
    /**
     * 获取电子秤读数
     * @param dto
     * @return
     */
    List<DpWeightVO> getWeighCount(TupleDTO<LocalDateTime,LocalDateTime> dto);

    /**
     * 合计每天的电子秤读数
     * @param dto
     * @return
     */
    List<DpValueDateVO> weightCountAsDay(TupleDTO<LocalDateTime,LocalDateTime> dto);

    /**
     * 拉引量熔化率
     * @param dto
     * @return
     */
    QtLineChartBarDTO weightCountDashBoard(DpPmsOperateDTO dto);
}
