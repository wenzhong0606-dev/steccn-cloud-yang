package com.steccn.dp.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.steccn.dp.domain.PointSiteMap;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Admin
* @description 针对表【point_site_map】的数据库操作Mapper
* @createDate 2024-08-21 14:38:45
* @Entity com.steccn.dp.domain.PointSiteMap
*/
@Mapper
@DS("ch")
public interface PointSiteMapMapper extends BaseMapper<PointSiteMap> {

}




