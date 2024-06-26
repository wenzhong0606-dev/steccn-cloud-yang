package com.steccn.dp.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.dp.domain.PullProduParDetail2;
import com.steccn.dp.service.PullProduParDetail2Service;
import com.steccn.dp.mapper.PullProduParDetail2Mapper;
import org.springframework.stereotype.Service;

/**
* @author Admin
* @description 针对表【pull_produ_par_detail2】的数据库操作Service实现
* @createDate 2024-06-26 15:57:07
*/
@Service
@DS("ch")
public class PullProduParDetail2ServiceImpl extends ServiceImpl<PullProduParDetail2Mapper, PullProduParDetail2>
    implements PullProduParDetail2Service{

}




