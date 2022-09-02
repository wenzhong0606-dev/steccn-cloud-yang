package com.steccn.pms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.pms.domain.PmProcessStation;
import com.steccn.pms.service.PmProcessStationService;
import com.steccn.pms.mapper.PmProcessStationMapper;
import org.springframework.stereotype.Service;

/**
* @author yequan
* @description 针对表【pm_process_station(生产工序执行工位表)】的数据库操作Service实现
* @createDate 2022-09-02 17:01:20
*/
@Service
public class PmProcessStationServiceImpl extends ServiceImpl<PmProcessStationMapper, PmProcessStation>
    implements PmProcessStationService{

}




