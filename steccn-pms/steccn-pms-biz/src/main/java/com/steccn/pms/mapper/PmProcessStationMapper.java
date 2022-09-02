package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmProcessStation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【pm_process_station(生产工序执行工位表)】的数据库操作Mapper
* @createDate 2022-09-02 17:01:20
* @Entity com.steccn.pms.domain.PmProcessStation
*/
@Mapper
public interface PmProcessStationMapper extends BaseMapper<PmProcessStation> {

}




