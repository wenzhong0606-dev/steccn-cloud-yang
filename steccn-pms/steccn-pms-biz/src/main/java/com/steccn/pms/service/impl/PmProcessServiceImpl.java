package com.steccn.pms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.pms.domain.PmProcess;
import com.steccn.pms.service.PmProcessService;
import com.steccn.pms.mapper.PmProcessMapper;
import org.springframework.stereotype.Service;

/**
* @author yequan
* @description 针对表【pm_process(生产工序表)】的数据库操作Service实现
* @createDate 2022-09-02 17:01:20
*/
@Service
public class PmProcessServiceImpl extends ServiceImpl<PmProcessMapper, PmProcess>
    implements PmProcessService{

}




