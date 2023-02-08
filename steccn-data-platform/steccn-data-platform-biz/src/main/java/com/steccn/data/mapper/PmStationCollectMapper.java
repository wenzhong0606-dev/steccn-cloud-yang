package com.steccn.data.mapper;

import com.steccn.data.domain.PmStationCollect;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.data.dto.PmReportWorkPreloadDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author yequan
* @description 针对表【pm_station_collect(生产管理-进出站计数器)】的数据库操作Mapper
* @createDate 2022-12-16 13:23:55
* @Entity com.steccn.data.domain.PmStationCollect
*/
@Mapper
public interface PmStationCollectMapper extends BaseMapper<PmStationCollect> {

    /**
     * 报工加工总数预填
     * @param dto
     * @return
     */
    List<PmStationCollect> rwPreLoad(@Param("dto") PmReportWorkPreloadDTO dto);
}




