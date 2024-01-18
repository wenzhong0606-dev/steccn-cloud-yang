package com.steccn.dp.service;

import com.steccn.common.dto.TupleDTO;
import com.steccn.dp.domain.AdsOnOffCount;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.dp.vo.DpOnOffCountVO;

import java.time.LocalDate;
import java.util.List;

/**
* @author Admin
* @description 针对表【ads_on_off_count(对开关数据的计数统计)】的数据库操作Service
* @createDate 2024-01-17 15:04:19
*/
public interface AdsOnOffCountService extends IService<AdsOnOffCount> {

    /**
     * 开关list
     * @param dto f1开始 f2结束
     * @return
     */
    List<DpOnOffCountVO> onOffList(TupleDTO<LocalDate,LocalDate> dto);
}
