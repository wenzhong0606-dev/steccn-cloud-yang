package com.steccn.dp.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.steccn.dp.domain.OpcUaDataCount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author Admin
* @description 针对表【opc_ua_data_count】的数据库操作Mapper
* @createDate 2024-08-21 14:36:31
* @Entity com.steccn.dp.domain.OpcUaDataCount
*/
@Mapper
@DS("ch")
public interface OpcUaDataCountMapper extends BaseMapper<OpcUaDataCount> {

    /**
     * 获取大类条目统计
     * @param time 时间
     * @param categoryCode 类别code
     * @return
     */
    List<OpcUaDataCount> detail(@Param("time") LocalDateTime time, @Param("category") String categoryCode);
}




