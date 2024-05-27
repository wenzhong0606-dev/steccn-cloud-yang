package com.steccn.dp.service;

import com.steccn.common.dto.TupleDTO;
import com.steccn.dp.domain.NaturalGasFlowCount;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.dp.vo.DpNatureGasVO;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author Admin
* @description 针对表【natural_gas_flow_count(天然气每小时流量均值统计)】的数据库操作Service
* @createDate 2024-05-27 15:57:48
*/
public interface NaturalGasFlowCountService extends IService<NaturalGasFlowCount> {
    /**
     * 获取时间内天然气读数
     * @param dto
     * @return
     */
    List<DpNatureGasVO> natureGas(TupleDTO<LocalDateTime,LocalDateTime> dto);
}
