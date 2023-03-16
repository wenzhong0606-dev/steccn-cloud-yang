package com.steccn.data.service;

import com.steccn.data.dto.CollectCommonDTO;
import com.steccn.data.dto.EpMeterCollectDTO;
import com.steccn.data.dto.PmStationCollectDTO;
import com.steccn.data.dto.QtThirdPartyDefectiveDTO;

/**
 * @author yequan
 * @desc 湖北博华采样数据接收接口
 */
public interface HbCollectService {

    /**
     * 保存推送过来的缺陷项
     * @param dto dto
     * @return true | false
     */
    Boolean saveDefective(QtThirdPartyDefectiveDTO dto);

    /**
     * 能源绩效-远传仪表
     * @param dto dto
     * @return true | false
     */
    Boolean epMeter(CollectCommonDTO<EpMeterCollectDTO> dto);

    /**
     * 生产管理-进出站计数器
     * @param dto dto
     * @return true | false
     */
    Boolean pmStation(CollectCommonDTO<PmStationCollectDTO> dto);
}
