package com.steccn.dp.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.dp.domain.PullProduParDetail1;
import com.steccn.dp.service.PullProduParDetail1Service;
import com.steccn.dp.mapper.PullProduParDetail1Mapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Service;

/**
* @author Admin
* @description 针对表【pull_produ_par_detail1】的数据库操作Service实现
* @createDate 2024-06-26 15:57:02
*/
@Service
@DS("ch")
public class PullProduParDetail1ServiceImpl extends ServiceImpl<PullProduParDetail1Mapper, PullProduParDetail1>
    implements PullProduParDetail1Service{

}




