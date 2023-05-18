package com.steccn.data.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.data.domain.PmStationCollect;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.data.dto.DwStationCollectDTO;
import com.steccn.data.dto.DwStationInfoDTO;
import com.steccn.data.dto.PmReportWorkPreloadDTO;
import com.steccn.data.vo.DwStationCollectVO;

import java.math.BigDecimal;
import java.util.List;

/**
* @author yequan
* @description 针对表【pm_station_collect(生产管理-进出站计数器)】的数据库操作Service
* @createDate 2022-12-16 13:23:55
*/
public interface PmStationCollectService extends IService<PmStationCollect> {

    /**
     * 生产管理-进出站计数归档
     * @param dto 请求对象
     * @return
     */
    List<DwStationCollectVO> stationCollect(DwStationCollectDTO dto);

    /**
     * 生产报工-加工总数预填
     * @param dto dto
     * @return
     */
    BigDecimal rwPreLoad(PmReportWorkPreloadDTO dto);


    /**
     * 生产管理-进出站计数信息
     * @param dto
     * @return
     */
    List<DwStationCollectVO> stationInfo(DwStationInfoDTO dto);
}
