package com.steccn.wms.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.wms.domain.WmsShelfUpStrategy;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.wms.dto.WmsShelfUpStrategyDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author W10
* @description 针对表【wms_shelf_up_strategy(上架策略)】的数据库操作Mapper
* @createDate 2022-11-30 09:45:17
* @Entity com.steccn.wms.domain.WmsShelfUpStrategy
*/
@Mapper
public interface WmsShelfUpStrategyMapper extends BaseMapper<WmsShelfUpStrategy> {

    List<WmsShelfUpStrategy> getShelfUpStrategyList(Page<WmsShelfUpStrategy> iPage, @Param("wmsShelfUpStrategyDTO") WmsShelfUpStrategyDTO dto);
}




