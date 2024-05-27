package com.steccn.dp.mapper;

import com.steccn.dp.domain.NaturalGasFlowCount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Admin
* @description 针对表【natural_gas_flow_count(天然气每小时流量均值统计)】的数据库操作Mapper
* @createDate 2024-05-27 15:57:48
* @Entity com.steccn.dp.domain.NaturalGasFlowCount
*/
@Mapper
public interface NaturalGasFlowCountMapper extends BaseMapper<NaturalGasFlowCount> {

}




