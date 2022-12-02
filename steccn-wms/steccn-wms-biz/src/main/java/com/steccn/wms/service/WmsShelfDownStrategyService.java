package com.steccn.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.vo.R;
import com.steccn.wms.domain.WmsShelfDownStrategy;
import com.steccn.wms.dto.WmsShelfDownStrategyDTO;
import com.steccn.wms.vo.WmsShelfDownStrategyVO;

/**
* @author W10
* @description 针对表【wms_shelf_down_strategy(下架策略)】的数据库操作Service
* @createDate 2022-11-30 09:45:17
*/
public interface WmsShelfDownStrategyService extends IService<WmsShelfDownStrategy> {

    Page<WmsShelfDownStrategyVO> getShelfDownStrategyList(WmsShelfDownStrategyDTO dto);

    R<Boolean> addOrUpdateShelfDownStrategy(WmsShelfDownStrategy dto);

    R<Boolean> delShelfDownStrategy( WmsShelfDownStrategy dto);

}
