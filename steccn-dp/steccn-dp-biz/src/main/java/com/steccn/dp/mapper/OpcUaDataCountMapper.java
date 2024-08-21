package com.steccn.dp.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.steccn.dp.domain.OpcUaDataCount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Admin
* @description 针对表【opc_ua_data_count】的数据库操作Mapper
* @createDate 2024-08-21 14:36:31
* @Entity com.steccn.dp.domain.OpcUaDataCount
*/
@Mapper
@DS("ch")
public interface OpcUaDataCountMapper extends BaseMapper<OpcUaDataCount> {

}




