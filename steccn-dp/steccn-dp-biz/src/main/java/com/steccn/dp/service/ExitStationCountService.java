package com.steccn.dp.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.dp.domain.ExitStationCount;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
* @author 34872
* @description 针对表【exit_station_count(出站量统计)】的数据库操作Service
* @createDate 2024-09-26 16:46:47
*/
public interface ExitStationCountService extends IService<ExitStationCount> {

     Map<Long, BigDecimal> output(Set<Long> orderIds);
}
