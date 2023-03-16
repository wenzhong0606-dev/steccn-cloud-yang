package com.steccn.data.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.data.domain.PmStationCollect;
import com.steccn.data.mapper.PmStationCollectMapper;
import com.steccn.data.service.PmStationCollectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author yequan
 * @description 针对表【pm_station_collect(生产管理-进出站计数器)】的数据库操作Service实现
 * @createDate 2022-12-16 13:23:55
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PmStationCollectServiceImpl extends ServiceImpl<PmStationCollectMapper, PmStationCollect>
        implements PmStationCollectService {
}




