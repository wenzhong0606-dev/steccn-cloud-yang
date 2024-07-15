package com.steccn.dp.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.steccn.dp.domain.PullProduParDetail2;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Admin
* @description 针对表【pull_produ_par_detail2】的数据库操作Mapper
* @createDate 2024-06-26 15:57:07
* @Entity com.steccn.dp.domain.PullProduParDetail2
*/
@Mapper
@DS("ch")
public interface PullProduParDetail2Mapper extends BaseMapper<PullProduParDetail2> {

}




