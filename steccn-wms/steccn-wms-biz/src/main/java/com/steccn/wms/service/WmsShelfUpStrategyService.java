package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsShelfUpStrategy;
import com.steccn.wms.dto.WmsShelfUpStrategyDTO;

/**
* @author W10
* @description 针对表【wms_shelf_up_strategy(上架策略)】的数据库操作Service
* @createDate 2022-11-30 09:45:17
*/
public interface WmsShelfUpStrategyService extends IService<WmsShelfUpStrategy> {

    Page<WmsShelfUpStrategy> getShelfUpStrategyList( WmsShelfUpStrategyDTO dto);

    R<Boolean> addOrUpdateShelfUpStrategy( WmsShelfUpStrategy dto);

    R<Boolean> delShelfUpStrategy( WmsShelfUpStrategy dto);
}
