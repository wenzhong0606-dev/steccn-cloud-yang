package com.steccn.dp.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.steccn.dp.domain.PullProduParDetail1;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Admin
* @description 针对表【pull_produ_par_detail1】的数据库操作Mapper
* @createDate 2024-06-26 15:57:02
* @Entity com.steccn.dp.domain.PullProduParDetail1
*/
@Mapper
@DS("ch")
public interface PullProduParDetail1Mapper extends BaseMapper<PullProduParDetail1> {

}




