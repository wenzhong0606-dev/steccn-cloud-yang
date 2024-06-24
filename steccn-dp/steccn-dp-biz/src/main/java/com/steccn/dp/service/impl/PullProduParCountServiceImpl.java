package com.steccn.dp.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.dp.domain.PullProduParCount;
import com.steccn.dp.service.PullProduParCountService;
import com.steccn.dp.mapper.PullProduParCountMapper;
import org.springframework.stereotype.Service;

/**
* @author Admin
* @description 针对表【pull_produ_par_count】的数据库操作Service实现
* @createDate 2024-06-24 16:15:14
*/
@Service
@DS("ch")
public class PullProduParCountServiceImpl extends ServiceImpl<PullProduParCountMapper, PullProduParCount>
    implements PullProduParCountService{

}




