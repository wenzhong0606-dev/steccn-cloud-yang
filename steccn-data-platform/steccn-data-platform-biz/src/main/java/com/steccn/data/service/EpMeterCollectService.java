package com.steccn.data.service;

import com.steccn.data.domain.EpMeterCollect;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.data.dto.DwConditionDTO;
import com.steccn.data.dto.DwEpMeterCollectVO;
import com.steccn.data.dto.DwMeterReadDTO;

import java.util.List;

/**
* @author yequan
* @description 针对表【ep_meter_collect(能源绩效-远程仪表采样表)】的数据库操作Service
* @createDate 2022-12-16 13:23:55
*/
public interface EpMeterCollectService extends IService<EpMeterCollect> {

    /**
     * 数仓条件列表
     * @param dwList
     * @return
     */
    List<DwEpMeterCollectVO> meterRead(List<DwMeterReadDTO> dwList);
}
